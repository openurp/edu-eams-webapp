package org.openurp.edu.eams.teach.grade.course.service

import java.util.Date



import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.Collections
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.transfer.TransferResult
import org.beangle.commons.transfer.importer.listener.ItemImporterListener
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson



class GradeImportListener(private var entityDao: EntityDao, private var project: Project, private var calculator: CourseGradeCalculator)
    extends ItemImporterListener {

  override def onFinish(tr: TransferResult) {
  }

  override def onItemStart(tr: TransferResult) {
  }

  override def onItemFinish(tr: TransferResult) {
    val courseGrade = populateCourseGrade(tr)
    if (!tr.hasErrors()) {
      try {
        if (!courseGrade.isPersisted) {
          courseGrade.setCreatedAt(new Date(System.currentTimeMillis()))
        }
        courseGrade.setUpdatedAt(new Date(System.currentTimeMillis()))
        if (null == courseGrade.getCreatedAt) {
          courseGrade.setCreatedAt(new Date(System.currentTimeMillis()))
        }
        entityDao.saveOrUpdate(courseGrade)
      } catch {
        case e: ConstraintViolationException => for (constraintViolation <- e.getConstraintViolations) {
          tr.addFailure(constraintViolation.getPropertyPath + constraintViolation.getMessage, constraintViolation.getInvalidValue)
        }
      }
    }
  }

  private def getPropEntity[T <: Entity[_]](clazz: Class[T], 
      tr: TransferResult, 
      key: String, 
      notNull: Boolean): T = {
    val description = importer.getDescriptions.get(key)
    val value = importer.getCurData.get(key).asInstanceOf[String]
    if (Strings.isBlank(value)) {
      if (notNull) {
        tr.addFailure(description + "不能为空", value)
      } else {
        return null
      }
    }
    val nameList = entityDao.get(clazz, "name", value)
    if (nameList.size != 1) {
      val codeList = entityDao.get(clazz, "code", value)
      if (codeList.size == 1) {
        return codeList.get(0)
      } else if ((nameList.size + codeList.size) == 0) {
        tr.addFailure(importer.getDescriptions.get(key) + "不存在", value)
      } else {
        tr.addFailure(importer.getDescriptions.get(key) + "存在多条记录", value)
      }
      return null
    }
    nameList.get(0)
  }

  private def checkCourseGradeExists(project: Project, 
      std: Student, 
      course: Course, 
      semester: Semester, 
      tr: TransferResult): CourseGrade = {
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std = :student", std)
    builder.where("courseGrade.course = :course", course)
    builder.where("courseGrade.semester = :semester", semester)
    builder.where("courseGrade.project = :project", project)
    var courseGrades: List[CourseGrade] = null
    val courseGrade = Model.newInstance(classOf[CourseGrade])
    try {
      courseGrades = entityDao.search(builder)
      if (courseGrades.size == 1) {
        return courseGrades.get(0)
      } else if (courseGrades.size > 1) {
        tr.addFailure("存在多条记录(", std.getName + "," + project.getName + "," + course.getName + 
          "," + 
          semester.getCode + 
          ")")
        return null
      }
      courseGrade.setStd(std)
      courseGrade.setProject(project)
      courseGrade.setSemester(semester)
      courseGrade.setCourse(course)
    } catch {
      case e: Exception => 
    }
    courseGrade
  }

  private def setExamGrades(courseGrade: CourseGrade, tr: TransferResult) {
    var examGrades = courseGrade.getExamGrades
    val gradeTypes = entityDao.getAll(classOf[GradeType])
    if (Collections.isEmpty(examGrades)) {
      examGrades = Collections.newSet[Any]
    }
    var examStatus = getPropEntity(classOf[ExamStatus], tr, "examStatus", false)
    if (examStatus == null) {
      examStatus = entityDao.get(classOf[ExamStatus], ExamStatus.NORMAL)
    }
    var scoreMarkStyle = getPropEntity(classOf[ScoreMarkStyle], tr, "scoreMarkStyle", false)
    if (scoreMarkStyle == null) {
      scoreMarkStyle = entityDao.get(classOf[ScoreMarkStyle], ScoreMarkStyle.PERCENT)
    }
    courseGrade.setMarkStyle(scoreMarkStyle)
    for (gradeType <- gradeTypes) {
      val value = importer.getCurData.get(gradeType.getCode).asInstanceOf[String]
      val examGrade = checkExamGradeExists(examGrades)
      checkGrade(value, examGrade, tr)
      examGrade.setGradeType(gradeType)
      examGrade.setExamStatus(examStatus)
      examGrade.setCourseGrade(courseGrade)
      examGrade.setMarkStyle(scoreMarkStyle)
      if (gradeType.id == GradeTypeConstants.GA_ID) {
        examGrade.setStatus(Grade.Status.PUBLISHED)
      }
    }
  }

  private def checkGrade(value: String, examGrade: ExamGrade, tr: TransferResult) {
    if (Strings.isNotEmpty(value)) {
      if (value.matches("^\\d*\\.?\\d*$") && java.lang.Float.parseFloat(value) <= 100) {
        examGrade.setScore(java.lang.Float.parseFloat(value))
      } else if (value.matches("^\\d*\\.?\\d*$") && java.lang.Float.parseFloat(value) > 100) {
        tr.addFailure("分数不能大于100", value)
      } else {
        examGrade.setScoreText(value)
      }
    }
  }

  private def checkExamGradeExists(examGrades: Set[ExamGrade]): ExamGrade = {
    var itor = examGrades.iterator()
    while (itor.hasNext) {
      return itor.next()
    }
    Model.newInstance(classOf[ExamGrade])
  }

  private def populateCourseGrade(tr: TransferResult): CourseGrade = {
    val std = getPropEntity(classOf[Student], tr, "student", true)
    val course = getPropEntity(classOf[Course], tr, "course", true)
    val semester = getPropEntity(classOf[Semester], tr, "semester", true)
    val courseGrade = checkCourseGradeExists(project, std, course, semester, tr)
    setExamGrades(courseGrade, tr)
    val lesson = getPropEntity(classOf[Lesson], tr, "lesson", false)
    if (null != lesson) {
      courseGrade.setLesson(lesson)
    }
    courseGrade.setLessonNo(importer.getCurData.get("lesson").asInstanceOf[String])
    val courseTakeType = getPropEntity(classOf[CourseTakeType], tr, "courseTakeType", false)
    if (null != courseTakeType) {
      courseGrade.setCourseTakeType(courseTakeType)
    }
    courseGrade.setStatus(Grade.Status.PUBLISHED)
    courseGrade
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }

  def setProject(project: Project) {
    this.project = project
  }
}
