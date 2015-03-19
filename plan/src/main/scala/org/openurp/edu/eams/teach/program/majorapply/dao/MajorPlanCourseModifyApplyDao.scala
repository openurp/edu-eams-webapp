package org.openurp.edu.eams.teach.program.majorapply.dao

import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailBeforeBean
//remove if not needed


trait MajorPlanCourseModifyApplyDao {

  def saveModifyApply(apply: MajorPlanCourseModifyBean, before: MajorPlanCourseModifyDetailBeforeBean, after: MajorPlanCourseModifyDetailAfterBean): Unit
}
