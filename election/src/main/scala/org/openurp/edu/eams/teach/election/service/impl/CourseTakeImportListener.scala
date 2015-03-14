package org.openurp.edu.eams.teach.election.service.impl

import java.text.MessageFormat
import java.util.List
import java.util.Map
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.TransferMessage
import org.beangle.commons.transfer.TransferResult
import org.beangle.commons.transfer.importer.listener.ItemImporterListener
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.dao.ElectionDao
import org.openurp.edu.eams.teach.election.service.helper.CourseLimitGroupHelper
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class CourseTakeImportListener(private var electionDao: ElectionDao, private var semester: Semester, private var project: Project)
    extends ItemImporterListener() {

  private var entityDao: EntityDao = electionDao.getEntityDao

  override def onItemStart(tr: TransferResult) {
  }

  override def onFinish(tr: TransferResult) {
  }

  override def onItemFinish(tr: TransferResult) {
    val errors = CollectUtils.newArrayList()
    val params = importer.getCurData
    val stdCode = params.get("std.code").asInstanceOf[String]
    val lessonNo = params.get("lesson.no").asInstanceOf[String]
    val courseTakeTypeCode = params.get("courseTakeType.code").asInstanceOf[String]
    if (Strings.isBlank(stdCode)) {
      addFailure(errors, tr, importer.getDescriptions.get("std.code") + "不能为空", "")
    }
    if (Strings.isBlank(lessonNo)) {
      addFailure(errors, tr, importer.getDescriptions.get("lesson.no") + "不能为空", "")
    }
    if (Strings.isBlank(courseTakeTypeCode)) {
      addFailure(errors, tr, importer.getDescriptions.get("courseTakeType.code") + 
        "不能为空", "")
    }
    if (hasErrors(errors, tr)) {
      return
    }
    var student: Student = null
    val stdQuery = OqlBuilder.from(classOf[Student], "std").cacheable()
    stdQuery.where("std.code = :code", stdCode).where("std.project = :project", project)
    val students = entityDao.search(stdQuery)
    if (CollectUtils.isEmpty(students)) {
      val msgfmt = "无法找到 {0}下 学号为{1}的学生"
      addFailure(errors, tr, MessageFormat.format(msgfmt, project.getName, stdCode), stdCode)
    } else {
      student = students.get(0)
    }
    if (hasErrors(errors, tr)) {
      return
    }
    var lesson: Lesson = null
    val lessonQuery = OqlBuilder.from(classOf[Lesson], "lesson").cacheable()
    lessonQuery.where("lesson.no = :lessonNo", lessonNo)
      .where("lesson.project = :project", project)
      .where("lesson.semester = :semester", semester)
    val lessons = entityDao.search(lessonQuery)
    if (CollectUtils.isEmpty(lessons)) {
      val msgfmt = "无法找到 {0} {1}-{2}学期下 序号为{3}的任务"
      addFailure(errors, tr, MessageFormat.format(msgfmt, project.getName, semester.getSchoolYear, semester.getName, 
        lessonNo), lessonNo)
    } else {
      lesson = lessons.get(0)
    }
    if (hasErrors(errors, tr)) {
      return
    }
    var courseTakeType: CourseTakeType = null
    val courseTakeTypes = entityDao.get(classOf[CourseTakeType], "code", courseTakeTypeCode)
    if (CollectUtils.isEmpty(courseTakeTypes)) {
      val msgfmt = "无法找到 代码为{0}的修读类别"
      addFailure(errors, tr, MessageFormat.format(msgfmt, courseTakeTypeCode), courseTakeTypeCode)
    } else {
      courseTakeType = courseTakeTypes.get(0)
    }
    if (hasErrors(errors, tr)) {
      return
    }
    val builder = OqlBuilder.from(classOf[CourseTake].getName, "take")
    builder.select("count(take.id)")
    builder.where("take.std.code = :stdCode", stdCode).where("take.lesson.semester = :semester", semester)
      .where("take.lesson.course = :course", lesson.getCourse)
    val count = entityDao.search(builder).iterator().next()
    if (count > 0) {
      val msgfmt = "在{0}-{1}学期下 {2}[{3}] 已经选过 {4}[{5}]"

      addFailure(errors, tr, MessageFormat.format(msgfmt, semester.getSchoolYear, semester.getName, student.getName, 
        student.getCode, lesson.getCourse.getName, lesson.getCourse.getCode), "")
    }
    if (hasErrors(errors, tr)) {
      return
    }
    val courseTake = Model.newInstance(classOf[CourseTake])
    courseTake.setCourseTakeType(courseTakeType)
    courseTake.setLesson(lesson)
    courseTake.setStd(student)
    courseTake.setElectionMode(Model.newInstance(classOf[ElectionMode], ElectionMode.ASSIGEND))
    importer.setCurrent(courseTake)
    try {
      electionDao.saveElection(courseTake, false)
    } catch {
      case e: ConstraintViolationException => for (constraintViolation <- e.getConstraintViolations) {
        addFailure(errors, tr, constraintViolation.getPropertyPath + constraintViolation.getMessage, 
          constraintViolation.getInvalidValue)
      }
    }
    hasErrors(errors, tr)
  }

  private def addFailure(errors: List[TransferMessage], 
      tr: TransferResult, 
      message: String, 
      value: AnyRef): Boolean = {
    errors.add(new TransferMessage(tr.getTransfer.getTranferIndex, message, value))
  }

  private def hasErrors(errors: List[TransferMessage], tr: TransferResult): Boolean = {
    if (!errors.isEmpty) {
      tr.getErrs.addAll(errors)
      return true
    }
    false
  }
}
