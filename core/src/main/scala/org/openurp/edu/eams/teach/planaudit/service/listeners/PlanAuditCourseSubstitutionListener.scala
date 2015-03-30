package org.openurp.edu.eams.teach.planaudit.service.listeners

import org.beangle.commons.collection.Collections
import org.openurp.edu.base.Course
import org.openurp.edu.teach.grade.domain.impl.GradeComparator
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.planaudit.CourseAuditResult
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.model.CourseAuditResultBean
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.eams.teach.planaudit.service.StdGrade
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CourseSubstitution
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.service.CourseSubstitutionService
import org.openurp.edu.eams.teach.time.util.TermCalculator
import PlanAuditCourseSubstitutionListener._

object PlanAuditCourseSubstitutionListener {

  private val substitutions_str = "substitutions"
}

class PlanAuditCourseSubstitutionListener extends PlanAuditListener {

  var courseSubstitutionService: CourseSubstitutionService = _

  private def getGroupKey(courseGroup: CourseGroup): String = courseGroup.name + "c"

  def startPlanAudit(context: PlanAuditContext): Boolean = {
    context.params.put(substitutions_str, courseSubstitutionService.getCourseSubstitutions(context.result.std))
    true
  }

  def endPlanAudit(context: PlanAuditContext) {
  }

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    val substituted = context.params.get(getGroupKey(planCourse.group)).asInstanceOf[collection.Set[Course]]
    !(substituted.contains(planCourse.course))
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    var substituted = context.params.get(getGroupKey(courseGroup)).asInstanceOf[collection.mutable.Set[Course]]
    if (null == substituted) {
      substituted = Collections.newSet[Course]
      context.params.put(getGroupKey(courseGroup), substituted)
    }
    val substitutions = context.params.get(substitutions_str).asInstanceOf[List[CourseSubstitution]]
    val stdGrade = context.stdGrade
    val courseMap = Collections.newMap[Course, PlanCourse]
    for (planCourse <- courseGroup.planCourses) {
      courseMap.put(planCourse.course, planCourse)
    }
    val auditTerms = context.auditTerms
    val needCheckTerm = (null != auditTerms && auditTerms.length >= 0)
    for (
      sc <- substitutions if sc.origins.subsetOf(courseMap.keySet) && isSubstitutes(stdGrade,
        sc)
    ) {
      val substituteGrades = Collections.newBuffer[CourseGrade]
      for (c <- sc.substitutes) {
        substituteGrades ++= stdGrade.getGrades(c)
        stdGrade.addNoGradeCourse(c)
      }
      for (ori <- sc.origins) {
        val planCourse = courseMap.get(ori).asInstanceOf[PlanCourse]
        var inTerm = !needCheckTerm
        if (needCheckTerm) {
          inTerm =
            (0 until auditTerms.length) exists { j => TermCalculator.inTerm(planCourse.terms, java.lang.Integer.valueOf(auditTerms(j))) }
        }
        if (inTerm) {
          val planCourseResult = new CourseAuditResultBean(planCourse)
          planCourseResult.checkPassed(stdGrade.getGrades(ori), substituteGrades)
          val tempStr = new StringBuffer()
          var iter = substituteGrades.iterator
          while (iter.hasNext) {
            val grade = iter.next()
            tempStr.append(grade.course.name).append('[')
              .append(grade.course.code)
              .append(']')
            if (iter.hasNext) {
              tempStr.append(',')
            }
          }
          planCourseResult.remark = tempStr.toString
          groupResult.addCourseResult(planCourseResult)
          groupResult.checkPassed(false)
          substituted.add(ori)
        }
      }
    }
    true
  }

  protected def isSubstitutes(stdGrade: StdGrade, substitution: CourseSubstitution): Boolean = {
    val allCourses = Collections.newSet[Course]
    allCourses ++= substitution.origins
    allCourses ++= substitution.substitutes
    val subGrades = Collections.newMap[Course, CourseGrade]
    for (course <- allCourses) {
      val grades = stdGrade.getGrades(course)
      if (Collections.isNotEmpty(grades)) subGrades.put(course, grades(0))
    }
    if (GradeComparator.isSubstitute(substitution, subGrades)) {
      for (course <- allCourses) stdGrade.useGrades(course)
      return true
    }
    false
  }
}
