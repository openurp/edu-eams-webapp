package org.openurp.edu.eams.teach.program.major.service


import com.ekingstar.eams.core.CommonAuditState
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.original.OriginalPlan
//remove if not needed


trait MajorPlanAuditService {

  def audit(plans: List[MajorPlan], auditState: CommonAuditState): Unit

  def submit(plans: List[MajorPlan]): Unit

  def revokeAccepted(plans: List[MajorPlan]): Unit

  def revokeSubmitted(plans: List[Program]): Unit

  def getOriginalMajorPlan(majorPlanId: java.lang.Long): OriginalPlan
}
