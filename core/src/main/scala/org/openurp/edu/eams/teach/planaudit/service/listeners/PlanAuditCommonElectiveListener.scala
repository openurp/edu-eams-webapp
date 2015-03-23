package org.openurp.edu.eams.teach.planaudit.service.listeners




import org.beangle.commons.collection.Collections
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.planaudit.CourseAuditResult
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.PlanAuditResult
import org.openurp.edu.teach.planaudit.model.CourseAuditResultBean
import org.openurp.edu.teach.planaudit.model.GroupAuditResultBean
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditListener
import org.openurp.edu.eams.teach.planaudit.service.StdGrade
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse



class PlanAuditCommonElectiveListener extends PlanAuditListener {

  def endPlanAudit(context: PlanAuditContext) {
    val result = context.result
    val stdGrade = context.stdGrade
    val electiveType = context.standard.convertTargetCourseType
    if (null == electiveType) return
    var groupResult = result.groupResult(electiveType)
    if (null == groupResult) {
      val groupRs = new GroupAuditResultBean()
      groupRs.courseType=electiveType
      groupRs.name=electiveType.name
      groupRs.groupNum = -1
      groupResult = groupRs
      result.addGroupResult(groupResult)
    }
    val restCourses = stdGrade.restCourses
    for (course <- restCourses) {
      val courseResult = new CourseAuditResultBean()
      courseResult.course=course
      val grades = stdGrade.useGrades(course)
      if (!grades.isEmpty && 
        grades.get(0).courseType.id != electiveType.id) {
        courseResult.remark="计划外"
      }
      courseResult.checkPassed(grades)
      groupResult.addCourseResult(courseResult)
    }
    processConvertCredits(groupResult, result, context)
    groupResult.checkPassed(true)
  }

  protected def processConvertCredits(target: GroupAuditResult, result: PlanAuditResult, context: PlanAuditContext) {
    val parents = Collections.newHashSet()
    val sibling = Collections.newHashSet()
    var start = target.parent
    while (null != start && !parents.contains(start)) {
      parents.add(start)
      start = start.parent
    }
    val parent = target.parent
    if (null != parent) {
      sibling.addAll(parent.children)
      sibling.remove(target)
    }
    var otherConverted = 0f
    var siblingConverted = 0f
    for (gr <- result.groupResults) {
      if (!context.standard.isConvertable(gr.courseType)) //continue
      if (gr == target || parents.contains(gr)) //continue
      if (sibling.contains(gr)) {
        siblingConverted += if (gr.isPassed) gr.auditStat.creditsCompleted - gr.auditStat.creditsRequired else 0f
      } else if (null == gr.parent) {
        otherConverted += if (gr.isPassed) gr.auditStat.creditsCompleted - gr.auditStat.creditsRequired else 0f
      }
    }
    target.auditStat.creditsConverted=(otherConverted + siblingConverted)
    for (r <- parents) r.auditStat.creditsConverted=otherConverted
  }

  def startPlanAudit(context: PlanAuditContext): Boolean = true

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean = {
    true
  }

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean = {
    true
  }
}
