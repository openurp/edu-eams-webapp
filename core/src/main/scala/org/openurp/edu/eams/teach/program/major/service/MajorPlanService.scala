package org.openurp.edu.eams.teach.program.major.service

import java.util.Collection
import java.util.List
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourseGroup

import scala.collection.JavaConversions._

trait MajorPlanService {

  def getPlanCourses(plan: MajorPlan): List[MajorPlanCourse]

  def getMajorPlanByAdminClass(clazz: Adminclass): MajorPlan

  def saveOrUpdateMajorPlan(plan: MajorPlan): Unit

  def removeMajorPlan(plan: MajorPlan): Unit

  def genMajorPlan(sourcePlan: MajorPlan, genParameter: MajorPlanGenParameter): CoursePlan

  def genMajorPlans(sourcePlans: Collection[MajorPlan], partialParams: MajorPlanGenParameter): List[MajorPlan]

  def getUnusedCourseTypes(plan: MajorPlan): List[CourseType]

  def statPlanCredits(planId: java.lang.Long): Float

  def statPlanCredits(plan: MajorPlan): Float

  def hasCourse(cgroup: MajorPlanCourseGroup, course: Course): Boolean

  def hasCourse(cgroup: MajorPlanCourseGroup, course: Course, planCourse: PlanCourse): Boolean
}
