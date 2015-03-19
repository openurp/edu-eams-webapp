package org.openurp.edu.eams.teach.program.share.service


import org.openurp.edu.eams.teach.program.share.SharePlan
//remove if not needed


trait SharePlanService {

  def getSharePlans(grade: String, educationId: java.lang.Long): List[SharePlan]
}
