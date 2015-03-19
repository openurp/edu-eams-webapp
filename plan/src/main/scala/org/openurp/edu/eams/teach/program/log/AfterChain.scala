package org.openurp.edu.eams.teach.program.log


import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


class AfterChain(someProperties: Map[String, String]) extends PhaseChain(someProperties) {

  def after(teachPlan: MajorPlan): InfoChain = {
    super.informations.put(MajorPlanLogHelper.AFTER, teachPlan.toString)
    new InfoChain(informations)
  }

  def after(courseGroup: MajorCourseGroup): InfoChain = {
    super.informations.put(MajorPlanLogHelper.AFTER, courseGroup.toString)
    new InfoChain(informations)
  }

  def after(planCourse: MajorPlanCourse): InfoChain = {
    super.informations.put(MajorPlanLogHelper.AFTER, planCourse.toString)
    new InfoChain(informations)
  }
}
