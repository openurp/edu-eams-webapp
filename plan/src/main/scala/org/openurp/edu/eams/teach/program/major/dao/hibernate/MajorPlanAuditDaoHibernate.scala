package org.openurp.edu.eams.teach.program.major.dao.hibernate

import org.openurp.edu.eams.teach.program.common.copydao.plan.IPlanCopyDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.dao.MajorPlanAuditDao
import org.openurp.edu.eams.teach.program.major.service.MajorPlanDuplicatedException
//remove if not needed


class MajorPlanAuditDaoHibernate extends MajorPlanAuditDao {

  private var originalMajorPlanCopyDao: IPlanCopyDao = _

  def accepted(plan: MajorPlan) {
    originalMajorPlanCopyDao.copyMajorPlan(plan, null)
  }

  def setOriginalMajorPlanCopyDao(originalMajorPlanCopyDao: IPlanCopyDao) {
    this.originalMajorPlanCopyDao = originalMajorPlanCopyDao
  }
}
