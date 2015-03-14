package org.openurp.edu.eams.teach.program.share.service

import java.util.List
import org.openurp.edu.eams.teach.program.share.SharePlan
//remove if not needed
import scala.collection.JavaConversions._

trait SharePlanService {

  def getSharePlans(grade: String, educationId: java.lang.Long): List[SharePlan]
}
