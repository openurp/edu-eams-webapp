package org.openurp.edu.eams.teach.grade.transcript.service.impl

import java.util.ArrayList
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.teach.plan.StdPlan
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider

import scala.collection.JavaConversions._

class TranscriptPlanCourseProvider extends BaseServiceImpl with TranscriptDataProvider {

  private var coursePlanProvider: CoursePlanProvider = _

  def getDataName(): String = "planCourses"

  def getData[T](std: Student, options: Map[String, String]): T = {
    val planCourses = getPlanCourses(std)
    if (CollectUtils.isNotEmpty(planCourses)) {
      return planCourses.get(0).asInstanceOf[T]
    }
    null.asInstanceOf[T]
  }

  def getDatas[T](stds: List[Student], options: Map[String, String]): Map[Student, T] = {
    val datas = CollectUtils.newHashMap()
    for (std <- stds) {
      val planCourses = getPlanCourses(std)
      datas.put(std, planCourses.asInstanceOf[T])
    }
    datas
  }

  private def getPlanCourses(std: Student): List[PlanCourse] = {
    val planCourses = new ArrayList[PlanCourse]()
    val personalPlan = coursePlanProvider.getPersonalPlan(std)
    if (personalPlan != null) {
      val courseGroups = personalPlan.getGroups
      for (courseGroup <- courseGroups if courseGroup != null) {
        planCourses.addAll(courseGroup.getPlanCourses)
      }
    }
    if (personalPlan == null) {
      val coursePlan = coursePlanProvider.majorPlan(std)
      if (coursePlan != null) {
        for (courseGroup <- coursePlan.getGroups) {
          planCourses.addAll(courseGroup.getPlanCourses)
        }
      }
    }
    planCourses
  }

  def setCoursePlanProvider(coursePlanProvider: CoursePlanProvider) {
    this.coursePlanProvider = coursePlanProvider
  }
}
