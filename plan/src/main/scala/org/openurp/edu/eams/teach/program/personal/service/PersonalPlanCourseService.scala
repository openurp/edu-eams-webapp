package org.openurp.edu.eams.teach.program.personal.service

import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlanCourse
//remove if not needed
import scala.collection.JavaConversions._

trait PersonalPlanCourseService {

  def removePlanCourse(planCourse: PersonalPlanCourse, plan: PersonalPlan): Unit

  def addPlanCourse(planCourse: PersonalPlanCourse, plan: PersonalPlan): Unit

  def updatePlanCourse(planCourse: PersonalPlanCourse, plan: PersonalPlan): Unit

  def getPersonalPlanCourseDwr(id: java.lang.Long): PersonalPlanCourse
}
