package org.openurp.edu.eams.teach.program.common.service



import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
//remove if not needed


trait PlanCompareService {

  def diff(leftPlan: CoursePlan, rightPlan: CoursePlan): Map[CourseType, Array[List[_ <: PlanCourse]]]
}
