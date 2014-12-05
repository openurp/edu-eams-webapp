package org.openurp.eams.grade.std.action

import org.beangle.webmvc.api.action.ActionSupport
import org.openurp.teach.grade.service.GpaStatService
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.teach.grade.CourseGrade
import org.openurp.teach.core.Student
import org.openurp.teach.code.GradeType
import org.openurp.teach.grade.service.GpaStatService
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.teach.grade.CourseGrade
import org.openurp.teach.core.Student
import org.openurp.teach.grade.Grade
import org.openurp.teach.grade.service.GpaStatService
import org.openurp.teach.core.Student
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.teach.grade.CourseGrade
import org.openurp.teach.grade.service.GpaStatService
import org.openurp.teach.code.service.BaseCodeService
import org.beangle.commons.collection.Order
import org.openurp.teach.grade.service.GpaStatService
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.teach.code.service.BaseCodeService
import org.openurp.base.Semester
/**
 * 学生查成绩
 * */
class IndexAction extends ActionSupport {

  var gpaStatService: GpaStatService = _
  var entityDao: EntityDao = _

  def index() = {
    val semesters = entityDao.getAll(classOf[Semester])
    put("semesters", semesters)
    val std = entityDao.get(classOf[Student], new java.lang.Long(46768))
    put("std", std)
    val semesterId = getInt("semester.id").getOrElse(0)
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std = :std", std)
    if (semesterId > 0)
      builder.where("courseGrade.semester.id = :semesterId", semesterId)
    // FIMXE 这里应该将没有考试成绩的情况考虑进去
    //    builder.where("courseGrade.status =:status or exists (from courseGrade.examGrades examGrade where examGrade.status = :status)",
    //      Grade.Status.Published)
    val courseGrades = entityDao.search(builder)
    put("grades", courseGrades)
    putGradeTypes()
    forward()
  }

  /**
   * 查看每学期成绩
   */
  def info(): String = {
    val semesterId = getInt("semester.id")
    val std = entityDao.get(classOf[Student], new java.lang.Long(46768))
    // 统计学生所选课程
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std = :std", std)
    builder.where("courseGrade.semester.id = :semesterId", semesterId)
    builder.where("courseGrade.status =:status", Grade.Status.Published)
    val courseGrades = entityDao.search(builder)
    put("grades", courseGrades)
    putGradeTypes()
    put("std", std)
    return forward()
  }

  def putGradeTypes() = {
    val gradeTypes = entityDao.getAll(classOf[GradeType])
    put("gradeTypes", gradeTypes)
    //		put("gradeTypes", BaseCodeService.getCodes(classOf[GradeType], GradeType.Usual ,
    //		        GradeType.Middle , GradeType.End , GradeType.Makeup ,
    //		        GradeType.Delay , GradeType.EndGa ))
  }

  def history(): String = {
    val std = entityDao.get(classOf[Student], new java.lang.Long(46768))
    put("stdGpa", gpaStatService.statGpa(std))
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std = :std", std)
    builder.where("courseGrade.status = :status", Grade.Status.Published)
    builder.orderBy(Order.parse("courseGrade.semester.beginOn desc"))
    put("grades", entityDao.search(builder))
    putGradeTypes()
    return forward()
  }

  def setGpaStatService(gpaStatService: GpaStatService) = {
    this.gpaStatService = gpaStatService
  }
}