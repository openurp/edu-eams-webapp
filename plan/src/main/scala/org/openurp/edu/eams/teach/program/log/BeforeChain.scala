package org.openurp.edu.eams.teach.program.log


import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


class BeforeChain(someProperties: Map[String, String]) extends PhaseChain(someProperties) {

  def skipBefore(): ResultChain = new ResultChain(informations)

  def before(plan: MajorPlan): ResultChain = {
    informations.put(MajorPlanLogHelper.BEFORE, plan.toString)
    new ResultChain(informations)
  }

  def before(courseGroup: MajorCourseGroup): ResultChain = {
    informations.put(MajorPlanLogHelper.BEFORE, courseGroup.toString)
    new ResultChain(informations)
  }

  def before(planCourse: MajorPlanCourse): ResultChain = {
    informations.put(MajorPlanLogHelper.BEFORE, planCourse.toString)
    new ResultChain(informations)
  }
}
