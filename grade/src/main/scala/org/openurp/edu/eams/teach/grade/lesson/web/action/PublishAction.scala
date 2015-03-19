package org.openurp.edu.eams.teach.grade.lesson.web.action


import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.query.LimitQuery
import org.beangle.commons.dao.query.QueryPage
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.struts2.convention.route.Action
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.service.impl.ExamTakeGeneratePublishListener
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class PublishAction extends SemesterSupportAction {

  protected var courseGradeService: CourseGradeService = _

  private var settings: CourseGradeSettings = _

  private var examTakeGeneratePublishListener: ExamTakeGeneratePublishListener = _

  def publish(): String = {
    val gradeTypeId = getInt("gradeTypeId")
    var gradeTypes: Array[GradeType] = null
    gradeTypes = if (null == gradeTypeId) baseCodeService.getCodes(classOf[GradeType]).toArray().asInstanceOf[Array[GradeType]] else if (settings.getSetting(getProject).getFinalCandinateTypes
      .contains(new GradeType(gradeTypeId))) entityDao.get(classOf[GradeType], Array(gradeTypeId, GradeTypeConstants.FINAL_ID))
      .toArray(Array.ofDim[GradeType](2)) else Array(baseCodeService.getCode(classOf[GradeType], gradeTypeId))
    courseGradeService.publish(get("lessonIds"), gradeTypes, true)
    val refer = new Action(Class.forName(get("refer")), "search")
    redirect(refer, "发布成功")
  }

  def genMakeupExamTake(): String = {
    val semesterId = getInt("semester.id")
    val projectId = getInt("project.id")
    val lessonNo = getLong("lesson.no")
    val builder = OqlBuilder.from(classOf[CourseGrade], "cg")
    builder.where("cg.semester.id=:semesterId and cg.std.project.id=:projectId ", semesterId, projectId)
      .where("cg.lesson is not null and cg.passed=false and cg.status=:status", Grade.Status.PUBLISHED)
    builder.orderBy("cg.lessonNo")
    if (null != lessonNo) builder.where("cg.lessonNo like :lessonNo", "%" + lessonNo + "%")
    builder.limit(new PageLimit(1, 100))
    val grades = new QueryPage[CourseGrade](builder.build().asInstanceOf[LimitQuery], entityDao)
    var size = 0
    val types = Array(new GradeType(GradeTypeConstants.GA_ID))
    for (grade <- grades) {
      val operations = examTakeGeneratePublishListener.onPublish(grade, types)
      if (!operations.isEmpty) {
        entityDao.execute(operations.toArray(Array.ofDim[Operation](operations.size)))
        size += operations.size
      }
    }
    put("size", size)
    forward()
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }

  def setSettings(settings: CourseGradeSettings) {
    this.settings = settings
  }

  def setExamTakeGeneratePublishListener(examTakeGeneratePublishListener: ExamTakeGeneratePublishListener) {
    this.examTakeGeneratePublishListener = examTakeGeneratePublishListener
  }
}
