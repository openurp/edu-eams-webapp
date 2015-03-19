package org.openurp.edu.eams.teach.program.common.copydao.plan

import java.util.Date
import org.beangle.commons.collection.CollectUtils
import com.ekingstar.eams.core.CommonAuditState
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.MajorPlanComment
import org.openurp.edu.eams.teach.program.major.model.MajorPlanBean
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
import org.openurp.edu.eams.teach.program.model.ProgramBean
//remove if not needed


class MajorPlanCopyDaoHibernate extends AbstractPlanCopyDao {

  protected override def newProgram(originalProgram: Program, genParameter: MajorPlanGenParameter): Program = {
    val newProgram = originalProgram.asInstanceOf[ProgramBean].clone().asInstanceOf[Program]
    val now = new Date()
    newProgram.setId(null)
    newProgram.setName(genParameter.getName)
    newProgram.setAuditState(CommonAuditState.UNSUBMITTED)
    newProgram.setCreatedAt(now)
    newProgram.setUpdatedAt(now)
    newProgram.setDuration(genParameter.getDuration)
    newProgram.setGrade(genParameter.getGrade)
    newProgram.setEffectiveOn(genParameter.getEffectiveOn)
    newProgram.setInvalidOn(genParameter.getInvalidOn)
    if (genParameter.getDegree == null || genParameter.getDegree.isTransient) {
      newProgram.setDegree(null)
    } else {
      newProgram.setDegree(genParameter.getDegree)
    }
    if (genParameter.getDepartment == null || genParameter.getDepartment.isTransient) {
      newProgram.setDepartment(null)
    } else {
      newProgram.setDepartment(genParameter.getDepartment)
    }
    if (genParameter.getDirection == null || genParameter.getDirection.isTransient) {
      newProgram.setDirection(null)
    } else {
      newProgram.setDirection(genParameter.getDirection)
    }
    if (genParameter.getEducation == null || genParameter.getEducation.isTransient) {
      newProgram.setEducation(null)
    } else {
      newProgram.setEducation(genParameter.getEducation)
    }
    if (genParameter.getMajor == null || genParameter.getMajor.isTransient) {
      newProgram.setMajor(null)
    } else {
      newProgram.setMajor(genParameter.getMajor)
    }
    if (genParameter.getStdType == null || genParameter.getStdType.isTransient) {
      genParameter.setStdType(null)
    } else {
      newProgram.setStdType(genParameter.getStdType)
    }
    if (genParameter.getStudyType == null || genParameter.getStudyType.isTransient) {
      newProgram.setStudyType(null)
    } else {
      newProgram.setStudyType(genParameter.getStudyType)
    }
    newProgram
  }

  protected override def newPlan(plan: MajorPlan): MajorPlan = {
    val mp = plan.asInstanceOf[MajorPlanBean].clone().asInstanceOf[MajorPlan]
    mp.setComments(CollectUtils.newArrayList[MajorPlanComment]())
    mp
  }
}
