package org.openurp.edu.eams.teach.program.majorapply.service

import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
//remove if not needed


trait MajorPlanCourseModifyAuditService {

  def approved(apply: MajorPlanCourseModifyBean, assessor: User): Unit

  def rejected(apply: MajorPlanCourseModifyBean, assessor: User): Unit
}
