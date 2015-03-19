package org.openurp.edu.eams.teach.program.share.service.impl


import org.openurp.edu.eams.teach.program.share.SharePlan
import org.openurp.edu.eams.teach.program.share.dao.SharePlanDao
import org.openurp.edu.eams.teach.program.share.service.SharePlanService

//remove if not needed


class SharePlanServiceImpl extends SharePlanService {

  
  var sharePlanDao: SharePlanDao = _

  def getSharePlans(grade: String, educationId: java.lang.Long): List[SharePlan] = {
    sharePlanDao.getSharePlans(grade, educationId)
  }
}
