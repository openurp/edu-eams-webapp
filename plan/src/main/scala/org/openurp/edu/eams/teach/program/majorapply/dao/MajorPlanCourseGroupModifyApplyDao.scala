package org.openurp.edu.eams.teach.program.majorapply.dao

import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailBeforeBean
//remove if not needed


trait MajorCourseGroupModifyApplyDao {

  def saveModifyApply(apply: MajorCourseGroupModifyBean, before: MajorCourseGroupModifyDetailBeforeBean, after: MajorCourseGroupModifyDetailAfterBean): Unit
}
