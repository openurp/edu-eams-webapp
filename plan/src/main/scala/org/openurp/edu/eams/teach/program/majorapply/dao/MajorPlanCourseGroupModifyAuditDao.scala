package org.openurp.edu.eams.teach.program.majorapply.dao

import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean
//remove if not needed
import scala.collection.JavaConversions._

trait MajorPlanCourseGroupModifyAuditDao {

  def approved(apply: MajorPlanCourseGroupModifyBean, assessor: User): Unit

  def rejected(apply: MajorPlanCourseGroupModifyBean, assessor: User): Unit
}
