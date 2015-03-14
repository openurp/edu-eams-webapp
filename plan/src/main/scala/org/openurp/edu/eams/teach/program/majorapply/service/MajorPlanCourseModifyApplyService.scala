package org.openurp.edu.eams.teach.program.majorapply.service

import java.util.List
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailBeforeBean
//remove if not needed
import scala.collection.JavaConversions._

trait MajorPlanCourseModifyApplyService {

  def saveModifyApply(apply: MajorPlanCourseModifyBean, before: MajorPlanCourseModifyDetailBeforeBean, after: MajorPlanCourseModifyDetailAfterBean): Unit

  def myApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseModifyBean]

  def myReadyModifyApply(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourse]

  def myReadyAddApplies(planId: java.lang.Long, userId: java.lang.Long): List[MajorPlanCourseModifyBean]

  def appliesOfPlan(planId: java.lang.Long): List[MajorPlanCourseModifyBean]
}
