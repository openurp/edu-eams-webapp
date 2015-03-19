package org.openurp.edu.eams.teach.lesson.task.biz


import org.beangle.commons.bean.comparators.PropertyComparator
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse




class CourseGroupPackage {

  
  var courseGroup: CourseGroup = _

  
  var credits: Float = _

  private var planCourses: List[PlanCourse] = new ArrayList[PlanCourse]()

  def getPlanCourses(): List[PlanCourse] = {
    if (!planCourses.isEmpty) {
      Collections.sort(planCourses, new PropertyComparator("course.code asc"))
    }
    planCourses
  }

  def setPlanCourses(planCourses: List[PlanCourse]) {
    this.planCourses = planCourses
  }
}
