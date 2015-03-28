package org.openurp.edu.eams.teach.program.common.service.impl






import org.apache.commons.collections.CollectionUtils
import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.common.service.PlanCompareService
import org.openurp.edu.eams.teach.program.common.service.helper.CourseTypeWrapper
import org.openurp.edu.eams.teach.program.common.service.helper.MajorPlanCourseWrapper
import org.openurp.edu.teach.plan.MajorPlanCourse
//remove if not needed


class PlanCompareServiceImpl extends BaseServiceImpl with PlanCompareService {

  def diff(leftMajorPlan: CoursePlan, rightMajorPlan: CoursePlan): Map[CourseType, Array[List[_ <: PlanCourse]]] = {
    val result = new HashMap[CourseType, Array[List[_ <: PlanCourse]]]()
    val leftCourseGroups = leftMajorPlan.getGroups
    val leftCourseTypes = new HashSet[CourseTypeWrapper]()
    for (courseGroup <- leftCourseGroups) {
      leftCourseTypes.add(new CourseTypeWrapper(courseGroup.getCourseType))
    }
    val rightCourseGroups = rightMajorPlan.getGroups
    val rightCourseTypes = new HashSet[CourseTypeWrapper]()
    for (courseGroup <- rightCourseGroups) {
      rightCourseTypes.add(new CourseTypeWrapper(courseGroup.getCourseType))
    }
    val onlyInLeftCourseTypes = unWrapCourseTypes(CollectionUtils.subtract(leftCourseTypes, rightCourseTypes))
    if (onlyInLeftCourseTypes.size > 0) {
      for (courseType <- onlyInLeftCourseTypes) {
        result.put(courseType, null)
        val planCourses = Array.ofDim[ArrayList](2)
        planCourses(0) = new ArrayList(leftMajorPlan.getGroup(courseType).getPlanCourses)
        Collections.sort(planCourses(0), MajorPlanCourseWrapper.COMPARATOR)
        planCourses(1) = new ArrayList()
        result.put(courseType, planCourses)
      }
    }
    val onlyInRightCourseTypes = unWrapCourseTypes(CollectionUtils.subtract(rightCourseTypes, leftCourseTypes))
    if (onlyInRightCourseTypes.size > 0) {
      for (courseType <- onlyInRightCourseTypes) {
        result.put(courseType, null)
        val planCourses = Array.ofDim[ArrayList](2)
        planCourses(0) = new ArrayList()
        planCourses(1) = new ArrayList(rightMajorPlan.getGroup(courseType).getPlanCourses)
        Collections.sort(planCourses(1), MajorPlanCourseWrapper.COMPARATOR)
        result.put(courseType, planCourses)
      }
    }
    val shareCourseTypes = unWrapCourseTypes(CollectionUtils.intersection(leftCourseTypes, rightCourseTypes))
    if (shareCourseTypes.size > 0) {
      for (courseType <- shareCourseTypes) {
        val wrappedLeftPlanCourses = wrapPlanCourses(leftMajorPlan.getGroup(courseType).getPlanCourses)
        val wrappedRightPlanCourses = wrapPlanCourses(rightMajorPlan.getGroup(courseType).getPlanCourses)
        val onlyInLeftPlanCourses = unWrapPlanCourses(CollectionUtils.subtract(wrappedLeftPlanCourses, 
          wrappedRightPlanCourses))
        val onlyInRightPlanCourses = unWrapPlanCourses(CollectionUtils.subtract(wrappedRightPlanCourses, 
          wrappedLeftPlanCourses))
        if (onlyInLeftPlanCourses.size != 0 || onlyInRightPlanCourses.size != 0) {
          result.put(courseType, null)
          val planCourses = Array.ofDim[ArrayList](2)
          planCourses(0) = new ArrayList(onlyInLeftPlanCourses)
          planCourses(1) = new ArrayList(onlyInRightPlanCourses)
          Collections.sort(planCourses(0), MajorPlanCourseWrapper.COMPARATOR)
          Collections.sort(planCourses(1), MajorPlanCourseWrapper.COMPARATOR)
          result.put(courseType, planCourses)
        }
      }
    }
    result
  }

  private def wrapPlanCourses(planCourses: Iterable[_ <: PlanCourse]): Iterable[MajorPlanCourseWrapper] = {
    Collections.collect(planCourses, MajorPlanCourseWrapper.WRAPPER)
  }

  private def unWrapPlanCourses(planCourseWrappers: Iterable[MajorPlanCourseWrapper]): Iterable[PlanCourse] = {
    Collections.collect(planCourseWrappers, MajorPlanCourseWrapper.UNWRAPPER)
  }

  private def wrapCourseTypes(courseTypes: Iterable[CourseType]): Iterable[CourseTypeWrapper] = {
    Collections.collect(courseTypes, CourseTypeWrapper.WRAPPER)
  }

  private def unWrapCourseTypes(courseTypeWrappers: Iterable[CourseTypeWrapper]): Iterable[CourseType] = {
    Collections.collect(courseTypeWrappers, CourseTypeWrapper.UNWRAPPER)
  }
}
