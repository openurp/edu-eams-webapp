package org.openurp.edu.eams.teach.program.common.copydao.plan

import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
import org.openurp.edu.eams.teach.program.original.model.OriginalPlanBean
//remove if not needed
import scala.collection.JavaConversions._

class OriginalMajorPlanCopyDaoHibernate extends AbstractPlanCopyDao {

  protected override def newProgram(originalProgram: Program, genParameter: MajorPlanGenParameter): Program = {
    originalProgram
  }

  protected override def newPlan(plan: MajorPlan): CoursePlan = {
    val oriPlan = new OriginalPlanBean()
    oriPlan.setCredits(plan.getCredits)
    oriPlan.setTermsCount(plan.getTermsCount)
    oriPlan
  }
}
