package org.openurp.edu.eams.teach.program.log


import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


class StartUpdateChain(informations: Map[String, String]) extends PhaseChain(informations) {

  informations.put(MajorPlanLogHelper.TYPE, "UPDATE")

  def start(plan: MajorPlan): BeforeChain = {
    initialLogInfo(plan)
    new BeforeChain(informations)
  }

  def start(courseGroup: MajorCourseGroup): BeforeChain = {
    initialLogInfo(courseGroup)
    new BeforeChain(informations)
  }

  def start(planCourse: MajorPlanCourse): BeforeChain = {
    initialLogInfo(planCourse)
    new BeforeChain(informations)
  }
}
