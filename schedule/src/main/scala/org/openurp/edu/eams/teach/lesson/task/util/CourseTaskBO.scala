package org.openurp.edu.eams.teach.lesson.task.util

import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class CourseTaskBO {

  @BeanProperty
  var semester: Semester = _

  private var teachPlan: MajorPlan = _

  private var adminClass: Adminclass = _

  @BeanProperty
  var planCourse: PlanCourse = _

  def getAdminclass(): Adminclass = adminClass

  def setAdminclass(adminClass: Adminclass) {
    this.adminClass = adminClass
  }

  def getMajorPlan(): MajorPlan = teachPlan

  def setMajorPlan(teachPlan: MajorPlan) {
    this.teachPlan = teachPlan
  }

  def this(teachPlan: MajorPlan, adminClass: Adminclass, planCourse: PlanCourse) {
    this()
    this.teachPlan = teachPlan
    this.adminClass = adminClass
    this.planCourse = planCourse
  }

  def this(teachPlan: MajorPlan, 
      adminClass: Adminclass, 
      planCourse: PlanCourse, 
      semester: Semester) {
    this()
    this.teachPlan = teachPlan
    this.adminClass = adminClass
    this.planCourse = planCourse
    this.semester = semester
  }

  def getTerm(): Int = {
    var term = 1
    if (semester != null) {
      val schoolyear = semester.getSchoolYear
      val name = semester.getName
      val beginYear = schoolyear.substring(0, 4)
      val beginYearInt = java.lang.Integer.valueOf(beginYear).intValue()
      val nameInt = java.lang.Integer.valueOf(name).intValue()
      val grade = teachPlan.getProgram.grade
      val year = grade.substring(0, 4)
      val flag = grade.substring(5, grade.length)
      val yearInt = java.lang.Integer.valueOf(year).intValue()
      if (flag == "9") {
        term = (beginYearInt - yearInt) * 2 + nameInt
      }
      if (flag == "3") {
        term = (beginYearInt - yearInt) * 2 + nameInt + 1
      }
    }
    term
  }

  def getTerm(semester: Semester): Int = {
    var term = 1
    val schoolyear = semester.getSchoolYear
    val name = semester.getName
    val beginYear = schoolyear.substring(0, 4)
    val beginYearInt = java.lang.Integer.valueOf(beginYear).intValue()
    val nameInt = java.lang.Integer.valueOf(name).intValue()
    val grade = teachPlan.getProgram.grade
    val year = grade.substring(0, 4)
    val flag = grade.substring(5, grade.length)
    val yearInt = java.lang.Integer.valueOf(year).intValue()
    if (flag == "9") {
      term = (beginYearInt - yearInt) * 2 + nameInt
    }
    if (flag == "3") {
      term = (beginYearInt - yearInt) * 2 + nameInt + 1
    }
    term
  }
}
