package org.openurp.edu.eams.teach.planaudit.service.internal

import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.teach.plan.CourseGroup



trait GroupResultBuilder {

  def buildResult(context: PlanAuditContext, group: CourseGroup): GroupAuditResult
}
