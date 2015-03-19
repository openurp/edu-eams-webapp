package org.openurp.edu.eams.teach.lesson.task.biz.comparator

import java.util.Comparator
import org.beangle.commons.lang.Objects
import org.openurp.edu.teach.plan.PlanCourse
import PlanCourseComparator._



object PlanCourseComparator {

  val COMPARATOR = new PlanCourseComparator()
}

class PlanCourseComparator private () extends Comparator[PlanCourse]() {

  def compare(o1: PlanCourse, o2: PlanCourse): Int = {
    Objects.compareBuilder().add(o1.getCourseGroup.getCourseType.getCode, o2.getCourseGroup.getCourseType.getCode)
      .add(o1.getCourse.getCode, o2.getCourse.getCode)
      .add(o1.id, o2.id)
      .toComparison()
  }
}
