package org.openurp.edu.eams.teach.program.majorapply.dao

import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
//remove if not needed


trait MajorPlanCourseModifyAuditDao {

  def approved(apply: MajorPlanCourseModifyBean, assessor: User): Unit

  def rejected(apply: MajorPlanCourseModifyBean, assessor: User): Unit
}
