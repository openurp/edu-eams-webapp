package org.openurp.edu.eams.teach.program.major.service

import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


trait MajorCourseGroupService {

  def saveOrUpdateCourseGroup(group: MajorCourseGroup): Unit

  def removeCourseGroup(groupId: java.lang.Long): Unit

  def removeCourseGroup(group: MajorCourseGroup): Unit

  @Deprecated
  def courseGroupMoveUp(courseGroup: MajorCourseGroup): Unit

  @Deprecated
  def courseGroupMoveDown(courseGroup: MajorCourseGroup): Unit

  def move(node: CourseGroup, location: CourseGroup, index: Int): Unit

  def hasSameGroupInOneLevel(courseGroup: CourseGroup, plan: CoursePlan, parent: CourseGroup): Boolean
}
