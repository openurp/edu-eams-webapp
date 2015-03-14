package org.openurp.edu.eams.teach.program.log

import java.util.Map
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
//remove if not needed
import scala.collection.JavaConversions._

class AfterChain(someProperties: Map[String, String]) extends PhaseChain(someProperties) {

  def after(teachPlan: MajorPlan): InfoChain = {
    super.informations.put(MajorPlanLogHelper.AFTER, teachPlan.toString)
    new InfoChain(informations)
  }

  def after(courseGroup: MajorPlanCourseGroup): InfoChain = {
    super.informations.put(MajorPlanLogHelper.AFTER, courseGroup.toString)
    new InfoChain(informations)
  }

  def after(planCourse: MajorPlanCourse): InfoChain = {
    super.informations.put(MajorPlanLogHelper.AFTER, planCourse.toString)
    new InfoChain(informations)
  }
}
