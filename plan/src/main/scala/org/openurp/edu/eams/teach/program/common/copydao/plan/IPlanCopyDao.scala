package org.openurp.edu.eams.teach.program.common.copydao.plan

import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
//remove if not needed


trait IPlanCopyDao {

  def copyMajorPlan(sourcePlan: MajorPlan, genParameter: MajorPlanGenParameter): CoursePlan
}
