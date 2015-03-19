package org.openurp.edu.eams.teach.program.share.dao


import org.openurp.edu.eams.teach.program.share.SharePlan
//remove if not needed


trait SharePlanDao {

  def getSharePlans(grade: String, educationId: java.lang.Long): List[SharePlan]
}
