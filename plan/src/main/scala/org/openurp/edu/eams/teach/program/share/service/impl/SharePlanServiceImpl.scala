package org.openurp.edu.eams.teach.program.share.service.impl

import java.util.List
import org.openurp.edu.eams.teach.program.share.SharePlan
import org.openurp.edu.eams.teach.program.share.dao.SharePlanDao
import org.openurp.edu.eams.teach.program.share.service.SharePlanService
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

class SharePlanServiceImpl extends SharePlanService {

  @BeanProperty
  var sharePlanDao: SharePlanDao = _

  def getSharePlans(grade: String, educationId: java.lang.Long): List[SharePlan] = {
    sharePlanDao.getSharePlans(grade, educationId)
  }
}
