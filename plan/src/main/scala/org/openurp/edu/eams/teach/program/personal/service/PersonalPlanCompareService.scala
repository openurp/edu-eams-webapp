package org.openurp.edu.eams.teach.program.personal.service



import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.exception.PersonalPlanSyncException
//remove if not needed


trait PersonalPlanCompareService {

  def diffPersonalAndMajorPlan(majorPlan: MajorPlan, stdMajorPlan: PersonalPlan): Map[CourseType, Array[List[_ <: PlanCourse]]]

  def copyPlanCourses(fromPlan: MajorPlan, toPlan: PersonalPlan, courseTypePlanCourseIds: List[Array[Number]]): Unit

  def copyCourseGroups(fromPlan: MajorPlan, toPlan: PersonalPlan, courseTypeIds: List[Integer]): Unit

  def deletePlanCourses(plan: PersonalPlan, courseTypePlanCourseIds: List[Array[Number]]): Unit

  def deleteCourseGroups(plan: PersonalPlan, courseTypeIds: List[Integer]): Unit
}
