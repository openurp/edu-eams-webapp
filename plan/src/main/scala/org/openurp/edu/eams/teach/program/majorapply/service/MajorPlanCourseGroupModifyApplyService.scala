package org.openurp.edu.eams.teach.program.majorapply.service

import java.util.List
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailAfterBean
//remove if not needed
import scala.collection.JavaConversions._

trait MajorPlanCourseGroupModifyApplyService {

  def saveModifyApply(modifyBean: MajorPlanCourseGroupModifyBean, courseGroupId: java.lang.Long, after: MajorPlanCourseGroupModifyDetailAfterBean): Unit

  def myApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseGroupModifyBean]

  def myReadyModifyApply(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseGroup]

  def myReadyAddApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseGroupModifyBean]

  def appliesOfPlan(planId: java.lang.Long): List[MajorPlanCourseGroupModifyBean]
}
