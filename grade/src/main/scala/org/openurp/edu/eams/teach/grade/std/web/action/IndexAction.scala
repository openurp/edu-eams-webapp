package org.openurp.edu.eams.teach.grade.std.web.action

import java.util.Collection
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.service.GpaStatService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class IndexAction extends SemesterSupportAction {

  protected var gpaStatService: GpaStatService = _

  protected override def indexSetting() {
    val std = getLoginStudent
    put("std", std)
    val semester = getAttribute("semester").asInstanceOf[Semester]
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std = :std", std)
    builder.where("courseGrade.semester = :semester", semester)
    builder.where("courseGrade.status =:status or exists (from courseGrade.examGrades examGrade where examGrade.status = :status)", 
      Grade.Status.PUBLISHED)
    val courseGrades = entityDao.search(builder)
    put("grades", courseGrades)
    putGradeTypes()
  }

  def info(): String = {
    val semesterId = getIntId("semester")
    val std = getLoginStudent
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std = :std", std)
    builder.where("courseGrade.semester.id = :semesterId", semesterId)
    builder.where("courseGrade.status =:status", Grade.Status.PUBLISHED)
    val courseGrades = entityDao.search(builder)
    put("grades", courseGrades)
    putGradeTypes()
    put("std", std)
    forward()
  }

  private def putGradeTypes() {
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType], GradeTypeConstants.USUAL_ID, GradeTypeConstants.MIDDLE_ID, 
      GradeTypeConstants.END_ID, GradeTypeConstants.MAKEUP_ID, GradeTypeConstants.DELAY_ID, GradeTypeConstants.GA_ID))
  }

  def history(): String = {
    val std = getLoginStudent
    put("stdGpa", gpaStatService.statGpa(std))
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std = :std", std)
    builder.where("courseGrade.status = :status", Grade.Status.PUBLISHED)
    builder.orderBy(Order.parse("courseGrade.semester.beginOn desc"))
    put("grades", entityDao.search(builder))
    putGradeTypes()
    forward()
  }

  def setGpaStatService(gpaStatService: GpaStatService) {
    this.gpaStatService = gpaStatService
  }
}
