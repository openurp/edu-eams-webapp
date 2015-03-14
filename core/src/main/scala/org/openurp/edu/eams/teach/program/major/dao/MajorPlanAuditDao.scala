package org.openurp.edu.eams.teach.program.major.dao

import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.program.major.service.MajorPlanDuplicatedException

import scala.collection.JavaConversions._

trait MajorPlanAuditDao {

  def accepted(plan: MajorPlan): Unit
}
