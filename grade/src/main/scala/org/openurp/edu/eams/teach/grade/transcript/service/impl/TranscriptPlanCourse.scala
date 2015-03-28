package org.openurp.edu.eams.teach.grade.transcript.service.impl




import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider



class TranscriptPlanCourse extends BaseServiceImpl with TranscriptDataProvider {

  private var coursePlanProvider: CoursePlanProvider = _

  def getDataName(): String = "planCourses"

  def getData[T](std: Student, options: Map[String, String]): T = {
    val planCourses = getPlanCourses(std)
    if (Collections.isNotEmpty(planCourses)) {
      return planCourses.get(0).asInstanceOf[T]
    }
    null.asInstanceOf[T]
  }

  def getDatas[T](stds: List[Student], options: Map[String, String]): Map[Student, T] = {
    val datas = Collections.newMap[Any]
    for (std <- stds) {
      val planCourses = getPlanCourses(std)
      datas.put(std, planCourses.asInstanceOf[T])
    }
    datas
  }

  private def getPlanCourses(std: Student): List[PlanCourse] = {
    val planCourses = new ArrayList[PlanCourse]()
    val coursePlan = coursePlanProvider.majorPlan(std)
    if (coursePlan != null) {
      for (courseGroup <- coursePlan.getGroups) {
        planCourses.addAll(courseGroup.getPlanCourses)
      }
    }
    planCourses
  }

  def setCoursePlanProvider(coursePlanProvider: CoursePlanProvider) {
    this.coursePlanProvider = coursePlanProvider
  }
}
