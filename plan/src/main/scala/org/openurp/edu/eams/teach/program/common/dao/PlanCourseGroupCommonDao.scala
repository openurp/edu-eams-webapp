package org.openurp.edu.eams.teach.program.common.dao

import java.util.List
import java.util.Set
import com.ekingstar.eams.teach.Course
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
//remove if not needed
import scala.collection.JavaConversions._

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

  def extractCourseInCourseGroup(group: MajorPlanCourseGroup, terms: String): List[Course]

  def extractPlanCourseInCourseGroup(group: MajorPlanCourseGroup, terms: Set[String]): List[MajorPlanCourse]

  def updateGroupTreeCredits(group: CourseGroup): Unit

  def getTopGroup(group: CourseGroup): CourseGroup
}
