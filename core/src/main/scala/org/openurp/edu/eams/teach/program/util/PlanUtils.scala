package org.openurp.edu.eams.teach.program.util

import org.beangle.commons.lang.Strings
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.base.Course
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.teach.plan.CourseGroup
import org.beangle.commons.collection.Collections

object PlanUtils {

  def getCourses(plan: MajorPlan, term: Int): Seq[Course] = {
    val courses = Collections.newSet[Course]
    for (planCourse <- getPlanCourses(plan)) {
      if (!Strings.isEmpty(planCourse.terms)) {

        if (planCourse.terms.indexOf("," + term + ",") != -1) {
          courses.add(planCourse.course)
        }
      }
    }
    courses.toList
  }

  def getPlanCourses(plan: MajorPlan): Seq[PlanCourse] = {
    if (Collections.isEmpty(plan.groups)) {
      return List.empty
    }
    val planCourses = Collections.newBuffer[PlanCourse]
    for (courseGroup <- plan.groups if null != courseGroup) {
      planCourses ++= courseGroup.planCourses
    }
    planCourses
  }

  def getUnPlannedPlanCourses(plan: MajorPlan): Seq[MajorPlanCourse] = {
    if (Collections.isEmpty(plan.groups)) {
      return List.empty
    }
    val planCourses = Collections.newBuffer[MajorPlanCourse]
    for (
      courseGroup <- plan.groups if courseGroup.planCourses != null && courseGroup.planCourses.size > 0;
      pcourse <- courseGroup.planCourses if isUnplannedTerm(pcourse.terms)
    ) {
      planCourses += (pcourse.asInstanceOf[MajorPlanCourse])
    }
    planCourses
  }

  def getPlannedCourse(plan: MajorPlan): Seq[MajorPlanCourse] = {
    if (Collections.isEmpty(plan.groups)) {
      return List.empty
    }
    val planCourses = Collections.newBuffer[MajorPlanCourse]
    for (
      courseGroup <- plan.groups if courseGroup.planCourses != null && courseGroup.planCourses.size > 0;
      pcourse <- courseGroup.planCourses if isUnplannedTerm(pcourse.terms)
    ) {
      planCourses += pcourse.asInstanceOf[MajorPlanCourse]
    }
    planCourses
  }

  private def isUnplannedTerm(term: String): Boolean = {
    Strings.contains(term, "*") || "春秋" == term
  }

  def getGroupCredits(group: CourseGroup, term: Int): Float = {
    val terms = group.termCredits.replaceAll("^,", "").replaceAll(",$", "")
      .split(",")
    if (term > terms.length) {
      return 0f
    }
    java.lang.Float.valueOf(terms(term - 1))
  }

  def getPlanCourses(plan: MajorPlan, term: Int): Seq[PlanCourse] = {
    val planCourses = Collections.newBuffer[PlanCourse]
    for (planCourse <- getPlanCourses(plan) if openOnThisTerm(planCourse.terms, term)) {
      planCourses += planCourse
    }
    planCourses
  }

  def getPlanCourses(group: CourseGroup, term: Int): Seq[PlanCourse] = {
    val result = Collections.newBuffer[PlanCourse]
    for (pCourse <- group.planCourses if openOnThisTerm(pCourse.terms, term)) {
      result += (pCourse)
    }
    result
  }

  def openOnThisTerm(terms: String, term: Int): Boolean = {
    if (Strings.isEmpty(terms)) {
      return false
    }
    if (terms == "春") {
      if (term % 2 == 0) {
        return true
      }
      return false
    }
    if (terms == "秋") {
      if (term % 1 == 0) {
        return true
      }
      return false
    }
    if (isUnplannedTerm(terms)) {
      return false
    }
    val termNumber = Strings.splitToInt(terms)
    for (t <- termNumber if t == term) {
      return true
    }
    false
  }

  def getPlanCoursesUntilTerm(courseGroup: CourseGroup, term: Int): Seq[PlanCourse] = {
    val result = Collections.newSet[PlanCourse]
    var i = 1
    while (i <= term) {
      result ++= (getPlanCourses(courseGroup, i))
      i += 1
    }
    result.toSeq
  }
}
