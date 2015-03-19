package org.openurp.edu.eams.teach.planaudit.service

import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse

trait PlanAuditListener {

  def startPlanAudit(context: PlanAuditContext): Boolean

  def startGroupAudit(context: PlanAuditContext, courseGroup: CourseGroup, groupResult: GroupAuditResult): Boolean

  def startCourseAudit(context: PlanAuditContext, groupResult: GroupAuditResult, planCourse: PlanCourse): Boolean

  def endPlanAudit(context: PlanAuditContext): Unit
}
