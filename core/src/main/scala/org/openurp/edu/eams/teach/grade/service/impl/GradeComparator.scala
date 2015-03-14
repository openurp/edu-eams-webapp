package org.openurp.edu.eams.teach.grade.service.impl

import java.util.Map
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.plan.CourseSubstitution

import scala.collection.JavaConversions._

object GradeComparator {

  def betterThan(first: CourseGrade, second: CourseGrade): Boolean = {
    if (null == second) return true
    val gp1 = if ((null == first.getGp)) 0 else first.getGp
    val gp2 = if ((null == second.getGp)) 0 else second.getGp
    val gpResult = java.lang.Float.compare(gp1, gp2)
    if (0 != gpResult) return gpResult > 0
    val score1 = if ((null == first.getScore)) 0 else first.getScore
    val score2 = if ((null == second.getScore)) 0 else second.getScore
    val scoreResult = java.lang.Float.compare(score1, score2)
    if (0 != scoreResult) return scoreResult > 0
    first.isPassed
  }

  def isSubstitute(substitution: CourseSubstitution, grades: Map[Course, CourseGrade]): Boolean = {
    var existOrigGrade = false
    var gpa1 = 0
    var ga1 = 0
    var credit1 = 0
    var passed1 = 0
    for (course <- substitution.getOrigins) {
      val grade = grades.get(course)
      if (null != grade) {
        if (grade.isPassed) passed1 += 1
        if (null != grade.getGp) gpa1 += grade.getCourse.getCredits * grade.getGp
        if (null != grade.getScore) ga1 = grade.getCourse.getCredits * grade.getScore
        existOrigGrade = true
      }
      credit1 += course.getCredits
    }
    var fullGrade2 = true
    var gpa2 = 0
    var ga2 = 0
    var credit2 = 0
    var passed2 = 0
    for (course <- substitution.getSubstitutes) {
      val grade = grades.get(course)
      if (null != grade) {
        if (grade.isPassed) passed2 += 1
        if (null != grade.getGp) gpa2 += grade.getCourse.getCredits * grade.getGp
        if (null != grade.getScore) ga2 = grade.getCourse.getCredits * grade.getScore
      } else {
        fullGrade2 = false
      }
      credit2 += course.getCredits
    }
    var success = false
    if (!existOrigGrade && fullGrade2) {
      success = true
    } else {
      if ((fullGrade2) && (credit1 > 0 && credit2 > 0)) {
        var gpaCompare = 0
        if (gpa1 > 0 || gpa2 > 0) {
          gpaCompare = java.lang.Float.compare(gpa1 / credit1, gpa2 / credit2)
        }
        if (0 == gpaCompare && (ga1 > 0 || ga2 > 0)) {
          gpaCompare = java.lang.Float.compare(ga1 / credit1, ga2 / credit2)
        }
        if (0 == gpaCompare) gpaCompare = passed1 - passed2
        success = gpaCompare <= 0
      }
    }
    success
  }
}
