package org.openurp.edu.eams.teach.lesson.task.util

import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan




class CourseTaskBO {

  
  var semester: Semester = _

  var teachPlan: MajorPlan = _

  var adminClass: Adminclass = _

  
  var planCourse: PlanCourse = _

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
      val schoolyear = semester.schoolYear
      val name = semester.name
      val beginYear = schoolyear.substring(0, 4)
      val beginYearInt = java.lang.Integer.valueOf(beginYear).intValue()
      val nameInt = java.lang.Integer.valueOf(name).intValue()
      val grade = teachPlan.program.grade
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
    val schoolyear = semester.schoolYear
    val name = semester.name
    val beginYear = schoolyear.substring(0, 4)
    val beginYearInt = java.lang.Integer.valueOf(beginYear).intValue()
    val nameInt = java.lang.Integer.valueOf(name).intValue()
    val grade = teachPlan.program.grade
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
