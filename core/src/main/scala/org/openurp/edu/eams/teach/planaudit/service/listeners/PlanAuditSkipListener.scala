package org.openurp.edu.eams.teach.planaudit.service.listeners




import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.model.PlanAuditStandard
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.util.PlanUtils



class PlanAuditSkipListener extends PlanAuditListener {

  def startPlanAudit(context: PlanAuditContext): Boolean = true

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    val standard = context.standard
    if (null == standard) {
      return true
    }
    val auditTerms = context.auditTerms
    if (auditTerms == null || auditTerms.length == 0) {
      return true
    }
    if (standard.isDisaudit(planCourse.courseGroup.courseType)) {
      return false
    }
    for (j <- 0 until auditTerms.length if PlanUtils.openOnThisTerm(planCourse.terms, java.lang.Integer.valueOf(auditTerms(j)))) {
      return true
    }
    false
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    val standard = context.standard
    if (null == standard) {
      return true
    }
    if (standard.isDisaudit(courseGroup.courseType)) {
      for (grade <- context.stdGrade.grades if standard.disauditCourseTypes.contains(grade.courseType)) {
        context.stdGrade.useGrades(grade.course)
      }
      val oriCreditsRequired = context.result.auditStat.creditsRequired
      val oriNumRequired = context.result.auditStat.numRequired
      context.result.auditStat.creditsRequired=(oriCreditsRequired - courseGroup.credits)
      context.result.auditStat.numRequired=(oriNumRequired - courseGroup.courseNum)
      return false
    }
    true
  }

  def endPlanAudit(context: PlanAuditContext) {
  }

  private def getPlanCourse(group: CourseGroup, course: Course): PlanCourse = {
    group.planCourses.find(_.course == course).orElse(null)
  }

  private def extractDisauditCourses(plan: CoursePlan, disauditCourseTypes: Set[CourseType]): Set[Course] = {
    val res = new HashSet[Course]()
    var iter = plan.groups.iterator()
    while (iter.hasNext) {
      val group = iter.next().asInstanceOf[CourseGroup]
      if (disauditCourseTypes.contains(group.courseType)) {
        res.addAll(extractDescendCourses(group))
      }
    }
    res
  }

  private def extractDescendCourses(group: CourseGroup): Set[Course] = {
    val res = new HashSet[Course]()
    for (pcourse <- group.planCourses) {
      res.add(pcourse.course)
    }
    for (child <- group.children) {
      res.addAll(extractDescendCourses(child))
    }
    res
  }
}
