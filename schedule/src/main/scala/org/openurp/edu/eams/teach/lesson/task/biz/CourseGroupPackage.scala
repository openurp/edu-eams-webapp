package org.openurp.edu.eams.teach.lesson.task.biz

import java.util.ArrayList
import java.util.Collections
import java.util.List
import org.beangle.commons.bean.comparators.PropertyComparator
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.PlanCourse
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class CourseGroupPackage {

  @BeanProperty
  var courseGroup: CourseGroup = _

  @BeanProperty
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
