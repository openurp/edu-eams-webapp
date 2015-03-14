package org.openurp.edu.eams.teach.planaudit.service.internal

import org.openurp.edu.eams.teach.planaudit.GroupAuditResult
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.program.CourseGroup

import scala.collection.JavaConversions._

trait GroupResultBuilder {

  def buildResult(context: PlanAuditContext, group: CourseGroup): GroupAuditResult
}
