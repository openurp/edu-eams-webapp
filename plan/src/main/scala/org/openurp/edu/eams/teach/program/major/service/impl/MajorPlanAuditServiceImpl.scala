package org.openurp.edu.eams.teach.program.major.service.impl


import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import com.ekingstar.eams.core.CommonAuditState
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.dao.MajorPlanAuditDao
import org.openurp.edu.eams.teach.program.major.service.MajorPlanAuditService
import org.openurp.edu.eams.teach.program.major.service.MajorPlanDuplicatedException
import org.openurp.edu.eams.teach.program.original.OriginalPlan
//remove if not needed


class MajorPlanAuditServiceImpl extends BaseServiceImpl with MajorPlanAuditService {

  private var majorPlanAuditDao: MajorPlanAuditDao = _

  def audit(plans: List[MajorPlan], auditState: CommonAuditState) {
    for (plan <- plans if canTransferTo(plan.getProgram.getAuditState, auditState)) {
      plan.getProgram.setAuditState(auditState)
      if (auditState == CommonAuditState.ACCEPTED) {
        majorPlanAuditDao.accepted(plan)
        entityDao.saveOrUpdate(plan.getProgram)
      } else {
        entityDao.saveOrUpdate(plan.getProgram)
      }
    }
  }

  def revokeAccepted(plans: List[MajorPlan]) {
    for (plan <- plans if canTransferTo(plan.getProgram.getAuditState, CommonAuditState.REJECTED) if plan.getProgram.getAuditState == CommonAuditState.ACCEPTED) {
      plan.getProgram.setAuditState(CommonAuditState.REJECTED)
      val originalPlans = entityDao.get(classOf[OriginalPlan], "program", plan.getProgram)
      entityDao.execute(Operation.remove(originalPlans).saveOrUpdate(plan))
    }
  }

  def submit(plans: List[MajorPlan]) {
    for (plan <- plans if canTransferTo(plan.getProgram.getAuditState, CommonAuditState.SUBMITTED)) {
      plan.getProgram.setAuditState(CommonAuditState.SUBMITTED)
    }
    entityDao.saveOrUpdate(plans)
  }

  def revokeSubmitted(plans: List[Program]) {
    for (program <- plans if canTransferTo(program.getAuditState, CommonAuditState.UNSUBMITTED)) {
      program.setAuditState(CommonAuditState.UNSUBMITTED)
    }
    entityDao.saveOrUpdate(plans)
  }

  private def canTransferTo(from: CommonAuditState, to: CommonAuditState): Boolean = from match {
    case UNSUBMITTED => if (to == CommonAuditState.SUBMITTED) {
      true
    } else {
      false
    }
    case SUBMITTED => if (to == CommonAuditState.ACCEPTED || to == CommonAuditState.REJECTED || 
      to == CommonAuditState.UNSUBMITTED) {
      true
    } else {
      false
    }
    case REJECTED => if (to == CommonAuditState.SUBMITTED) {
      true
    } else {
      false
    }
    case ACCEPTED => if (to == CommonAuditState.REJECTED) {
      true
    } else {
      false
    }
    case _ => false
  }

  def getOriginalMajorPlan(majorPlanId: java.lang.Long): OriginalPlan = {
    val query = OqlBuilder.from(classOf[OriginalPlan], "plan")
    query.where("plan.program.id=(select mp.program from org.openurp.edu.eams.teach.program.major.MajorPlan mp where mp.id = :mplanid)", 
      majorPlanId)
    val originalPlans = entityDao.search(query)
    if (originalPlans == null || originalPlans.size == 0) {
      throw new RuntimeException("Cannot find Original Plan")
    }
    if (originalPlans.size > 1) {
      throw new RuntimeException("Error More than one Original Plan found")
    }
    originalPlans.get(0)
  }

  def setMajorPlanAuditDao(majorPlanAuditDao: MajorPlanAuditDao) {
    this.majorPlanAuditDao = majorPlanAuditDao
  }
}
