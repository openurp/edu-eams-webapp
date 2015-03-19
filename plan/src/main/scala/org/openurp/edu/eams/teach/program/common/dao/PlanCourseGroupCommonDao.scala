package org.openurp.edu.eams.teach.program.common.dao



import com.ekingstar.eams.teach.Course
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


trait PlanCourseGroupCommonDao {

  def saveOrUpdateCourseGroup(group: CourseGroup): Unit

  @Deprecated
  def addCourseGroupToPlan(group: CourseGroup, plan: CoursePlan): Unit

  def addCourseGroupToPlan(group: CourseGroup, parent: CourseGroup, plan: CoursePlan): Unit

  def removeCourseGroup(group: CourseGroup): Unit

  @Deprecated
  def updateCourseGroupMoveDown(courseGroup: CourseGroup): Unit

  @Deprecated
  def updateCourseGroupMoveUp(courseGroup: CourseGroup): Unit

  def getCourseGroupByCourseType(planGroup: CourseGroup, planId: java.lang.Long, courseTypeId: java.lang.Integer): CourseGroup

  @Deprecated
  def updateCourseGroupParent(group: CourseGroup, newParent: CourseGroup, plan: CoursePlan): Unit

  def extractCourseInCourseGroup(group: MajorCourseGroup, terms: String): List[Course]

  def extractPlanCourseInCourseGroup(group: MajorCourseGroup, terms: Set[String]): List[MajorPlanCourse]

  def updateGroupTreeCredits(group: CourseGroup): Unit

  def getTopGroup(group: CourseGroup): CourseGroup
}
