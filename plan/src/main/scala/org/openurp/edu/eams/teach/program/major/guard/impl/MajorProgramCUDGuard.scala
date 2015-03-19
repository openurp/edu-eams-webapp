package org.openurp.edu.eams.teach.program.major.guard.impl


import com.ekingstar.eams.core.CommonAuditState
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.exception.MajorProgramGuardException
import org.openurp.edu.eams.teach.program.major.guard.MajorProgramOperateType
//remove if not needed


class MajorProgramCUDGuard extends AbstractMajorProgramGuard {

  protected override def iWantGuard(operType: MajorProgramOperateType): Boolean = {
    if (operType == MajorProgramOperateType.CREATE || operType == MajorProgramOperateType.UPDATE || 
      operType == MajorProgramOperateType.DELETE) {
      return true
    }
    false
  }

  protected override def doGuard(operType: MajorProgramOperateType, program: Program, context: Map[String, Any]) {
    if (program.getAuditState == CommonAuditState.ACCEPTED) {
      throw new MajorProgramGuardException("专业培养计划已审核通过，不能操作")
    }
    if (program.getAuditState == CommonAuditState.SUBMITTED) {
      throw new MajorProgramGuardException("专业培养计划已提交，正在审核中，不能操作")
    }
  }
}
