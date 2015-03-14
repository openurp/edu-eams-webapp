package org.openurp.edu.eams.teach.planaudit.service.listeners

import java.util.HashSet
import java.util.Iterator
import java.util.Set
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.planaudit.GroupAuditResult
import org.openurp.edu.eams.teach.planaudit.model.PlanAuditStandard
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.program.util.PlanUtils

import scala.collection.JavaConversions._

class PlanAuditSkipListener extends PlanAuditListener {

  def startPlanAudit(context: PlanAuditContext): Boolean = true

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    val standard = context.getStandard
    if (null == standard) {
      return true
    }
    val auditTerms = context.getAuditTerms
    if (auditTerms == null || auditTerms.length == 0) {
      return true
    }
    if (standard.isDisaudit(planCourse.getCourseGroup.getCourseType)) {
      return false
    }
    for (j <- 0 until auditTerms.length if PlanUtils.openOnThisTerm(planCourse.getTerms, java.lang.Integer.valueOf(auditTerms(j)))) {
      return true
    }
    false
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    val standard = context.getStandard
    if (null == standard) {
      return true
    }
    if (standard.isDisaudit(courseGroup.getCourseType)) {
      for (grade <- context.getStdGrade.grades if standard.getDisauditCourseTypes.contains(grade.getCourseType)) {
        context.getStdGrade.useGrades(grade.getCourse)
      }
      val oriCreditsRequired = context.getResult.getAuditStat.getCreditsRequired
      val oriNumRequired = context.getResult.getAuditStat.getNumRequired
      context.getResult.getAuditStat.setCreditsRequired(oriCreditsRequired - courseGroup.getCredits)
      context.getResult.getAuditStat.setNumRequired(oriNumRequired - courseGroup.getCourseNum)
      return false
    }
    true
  }

  def endPlanAudit(context: PlanAuditContext) {
  }

  private def getPlanCourse(group: CourseGroup, course: Course): PlanCourse = {
    group.getPlanCourses.find(_.getCourse == course).getOrElse(null)
  }

  private def extractDisauditCourses(plan: CoursePlan, disauditCourseTypes: Set[CourseType]): Set[Course] = {
    val res = new HashSet[Course]()
    var iter = plan.getGroups.iterator()
    while (iter.hasNext) {
      val group = iter.next().asInstanceOf[CourseGroup]
      if (disauditCourseTypes.contains(group.getCourseType)) {
        res.addAll(extractDescendCourses(group))
      }
    }
    res
  }

  private def extractDescendCourses(group: CourseGroup): Set[Course] = {
    val res = new HashSet[Course]()
    for (pcourse <- group.getPlanCourses) {
      res.add(pcourse.getCourse)
    }
    for (child <- group.getChildren) {
      res.addAll(extractDescendCourses(child))
    }
    res
  }
}
