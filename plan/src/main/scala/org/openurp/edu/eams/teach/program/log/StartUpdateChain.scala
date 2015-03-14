package org.openurp.edu.eams.teach.program.log

import java.util.Map
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
//remove if not needed
import scala.collection.JavaConversions._

class StartUpdateChain(informations: Map[String, String]) extends PhaseChain(informations) {

  informations.put(MajorPlanLogHelper.TYPE, "UPDATE")

  def start(plan: MajorPlan): BeforeChain = {
    initialLogInfo(plan)
    new BeforeChain(informations)
  }

  def start(courseGroup: MajorPlanCourseGroup): BeforeChain = {
    initialLogInfo(courseGroup)
    new BeforeChain(informations)
  }

  def start(planCourse: MajorPlanCourse): BeforeChain = {
    initialLogInfo(planCourse)
    new BeforeChain(informations)
  }
}
