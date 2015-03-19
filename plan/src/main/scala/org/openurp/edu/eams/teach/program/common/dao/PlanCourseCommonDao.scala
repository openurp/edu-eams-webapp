package org.openurp.edu.eams.teach.program.common.dao

import com.ekingstar.eams.teach.Course
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
//remove if not needed


trait PlanCourseCommonDao {

  def addPlanCourse(planCourse: PlanCourse, plan: CoursePlan): Unit

  def removePlanCourse(planCourse: PlanCourse, plan: CoursePlan): Unit

  def updatePlanCourse(planCourse: PlanCourse, plan: CoursePlan): Unit

  def getMajorPlanCourseByCourse(majorPlan: MajorPlan, course: Course): MajorPlanCourse
}
