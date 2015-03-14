package org.openurp.edu.eams.teach.program.share.dao.hibernate

import java.util.List
import org.beangle.orm.hibernate.HibernateEntityDao
import org.openurp.edu.eams.teach.program.share.SharePlan
import org.openurp.edu.eams.teach.program.share.dao.SharePlanDao
//remove if not needed
import scala.collection.JavaConversions._

class SharePlanDaoHibernate extends HibernateEntityDao with SharePlanDao {

  def getSharePlans(grade: String, educationId: java.lang.Long): List[SharePlan] = {
    var str = "from SharePlan sharePlan where sharePlan.grade='" + grade + 
      "'"
    if (educationId != null && "" != educationId) {
      str += " and sharePlan.education.id=" + educationId
    }
    getSession.createQuery(str).list()
  }
}
