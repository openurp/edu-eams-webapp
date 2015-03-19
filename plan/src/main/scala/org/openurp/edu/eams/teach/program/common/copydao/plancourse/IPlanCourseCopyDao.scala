package org.openurp.edu.eams.teach.program.common.copydao.plancourse


import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
//remove if not needed


trait IPlanCourseCopyDao {

  def copyPlanCourses(sourcePlanCourses: List[_ <: PlanCourse], courseGroupAttachTo: CourseGroup): List[_ <: PlanCourse]

  def copyPlanCourse(sourcePlanCourse: PlanCourse, courseGroupAttachTo: CourseGroup): PlanCourse
}
