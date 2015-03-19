package org.openurp.edu.eams.teach.program.majorapply.service


import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailAfterBean
//remove if not needed


trait MajorCourseGroupModifyApplyService {

  def saveModifyApply(modifyBean: MajorCourseGroupModifyBean, courseGroupId: java.lang.Long, after: MajorCourseGroupModifyDetailAfterBean): Unit

  def myApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorCourseGroupModifyBean]

  def myReadyModifyApply(planId: java.lang.Long, userId: java.lang.Long): List[MajorCourseGroup]

  def myReadyAddApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorCourseGroupModifyBean]

  def appliesOfPlan(planId: java.lang.Long): List[MajorCourseGroupModifyBean]
}
