package org.openurp.edu.eams.teach.lesson.task.biz


import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
import org.springframework.beans.support.PropertyComparator
import org.beangle.commons.collection.Collections




class CourseGroupPackage {

  
  var courseGroup: CourseGroup = _

  
  var credits: Float = _

  var planCourses = Collections.newBuffer[PlanCourse]

//  def getPlanCourses(): List[PlanCourse] = {
//    if (!planCourses.isEmpty) {
//      Collections.sort(planCourses, new PropertyComparator("course.code asc"))
//    }
//    planCourses
//  }
}
