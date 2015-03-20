package org.openurp.edu.eams.teach.program.major.service



import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup



trait MajorPlanService {

  def getPlanCourses(plan: MajorPlan): List[MajorPlanCourse]

  def getMajorPlanByAdminClass(clazz: Adminclass): MajorPlan

  def saveOrUpdateMajorPlan(plan: MajorPlan): Unit

  def removeMajorPlan(plan: MajorPlan): Unit

  def genMajorPlan(sourcePlan: MajorPlan, genParameter: MajorPlanGenParameter): CoursePlan

  def genMajorPlans(sourcePlans: Iterable[MajorPlan], partialParams: MajorPlanGenParameter): List[MajorPlan]

  def getUnusedCourseTypes(plan: MajorPlan): List[CourseType]

  def statPlanCredits(planId: java.lang.Long): Float

  def statPlanCredits(plan: MajorPlan): Float

  def hasCourse(cgroup: MajorCourseGroup, course: Course): Boolean

  def hasCourse(cgroup: MajorCourseGroup, course: Course, planCourse: PlanCourse): Boolean
}
