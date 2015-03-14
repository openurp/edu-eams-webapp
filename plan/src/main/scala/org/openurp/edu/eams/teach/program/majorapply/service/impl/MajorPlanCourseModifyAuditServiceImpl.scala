package org.openurp.edu.eams.teach.program.majorapply.service.impl

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorPlanCourseModifyAuditDao
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseModifyAuditService
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseModifyAuditServiceImpl extends BaseServiceImpl with MajorPlanCourseModifyAuditService {

  private var majorPlanCourseModifyAuditDao: MajorPlanCourseModifyAuditDao = _

  def approved(apply: MajorPlanCourseModifyBean, assessor: User) {
    majorPlanCourseModifyAuditDao.approved(apply, assessor)
  }

  def rejected(apply: MajorPlanCourseModifyBean, assessor: User) {
    majorPlanCourseModifyAuditDao.rejected(apply, assessor)
  }

  def setMajorPlanCourseModifyAuditDao(majorPlanCourseModifyAuditDao: MajorPlanCourseModifyAuditDao) {
    this.majorPlanCourseModifyAuditDao = majorPlanCourseModifyAuditDao
  }
}
