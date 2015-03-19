package org.openurp.edu.eams.teach.program.majorapply.service

import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean
//remove if not needed


trait MajorCourseGroupModifyAuditService {

  def approved(apply: MajorCourseGroupModifyBean, assessor: User): Unit

  def rejected(apply: MajorCourseGroupModifyBean, assessor: User): Unit
}
