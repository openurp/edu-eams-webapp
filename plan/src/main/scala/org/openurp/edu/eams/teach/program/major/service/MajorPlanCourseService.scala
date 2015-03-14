package org.openurp.edu.eams.teach.program.major.service

import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
//remove if not needed
import scala.collection.JavaConversions._

trait MajorPlanCourseService {

  def removePlanCourse(planCourse: MajorPlanCourse, plan: MajorPlan): Unit

  def addPlanCourse(planCourse: MajorPlanCourse, plan: MajorPlan): Unit

  def updatePlanCourse(planCourse: MajorPlanCourse, plan: MajorPlan): Unit

  def getMajorPlanCourseDwr(id: java.lang.Long): MajorPlanCourse
}
