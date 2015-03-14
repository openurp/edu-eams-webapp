package org.openurp.edu.eams.teach.program.major.service

import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
//remove if not needed
import scala.collection.JavaConversions._

trait MajorPlanCourseGroupService {

  def saveOrUpdateCourseGroup(group: MajorPlanCourseGroup): Unit

  def removeCourseGroup(groupId: java.lang.Long): Unit

  def removeCourseGroup(group: MajorPlanCourseGroup): Unit

  @Deprecated
  def courseGroupMoveUp(courseGroup: MajorPlanCourseGroup): Unit

  @Deprecated
  def courseGroupMoveDown(courseGroup: MajorPlanCourseGroup): Unit

  def move(node: CourseGroup, location: CourseGroup, index: Int): Unit

  def hasSameGroupInOneLevel(courseGroup: CourseGroup, plan: CoursePlan, parent: CourseGroup): Boolean
}
