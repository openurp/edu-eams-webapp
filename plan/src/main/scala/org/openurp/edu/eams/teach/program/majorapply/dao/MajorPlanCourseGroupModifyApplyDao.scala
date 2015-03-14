package org.openurp.edu.eams.teach.program.majorapply.dao

import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailBeforeBean
//remove if not needed
import scala.collection.JavaConversions._

trait MajorPlanCourseGroupModifyApplyDao {

  def saveModifyApply(apply: MajorPlanCourseGroupModifyBean, before: MajorPlanCourseGroupModifyDetailBeforeBean, after: MajorPlanCourseGroupModifyDetailAfterBean): Unit
}
