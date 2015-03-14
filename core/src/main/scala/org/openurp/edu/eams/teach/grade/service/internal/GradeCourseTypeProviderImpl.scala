package org.openurp.edu.eams.teach.grade.service.internal

import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.grade.service.GradeCourseTypeProvider
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import org.openurp.edu.eams.teach.program.share.SharePlan

import scala.collection.JavaConversions._

class GradeCourseTypeProviderImpl extends BaseServiceImpl with GradeCourseTypeProvider {

  var coursePlanProvider: CoursePlanProvider = _

  def getCourseType(std: Student, course: Course, defaultCourseType: CourseType): CourseType = {
    val plan = coursePlanProvider.getCoursePlan(std)
    var planCourseType: CourseType = null
    if (null != plan) {
      for (cg <- plan.getGroups) {
        if (cg == null) {
          //continue
        }
        for (pc <- cg.getPlanCourses if pc.getCourse == course) {
          planCourseType = cg.getCourseType
          //break
        }
      }
    }
    if (null == planCourseType) {
      val grade = java.lang.Integer.valueOf(std.grade.substring(0, 4))
      val builder = OqlBuilder.from(classOf[SharePlan], "sp").join("sp.groups", "spg")
        .join("spg.planCourses", "spgp")
        .where("spgp.course=:course", course)
        .where("sp.project=:project", std.getProject)
        .where("year(sp.effectiveOn)<=:grade and (sp.invalidOn is null or year(sp.invalidOn)>=:grade)", 
        grade)
        .select("spg.courseType")
      val types = entityDao.search(builder)
      if (!types.isEmpty) planCourseType = types.get(0)
    }
    if (null == planCourseType) planCourseType = defaultCourseType
    planCourseType
  }

  def setCoursePlanProvider(coursePlanProvider: CoursePlanProvider) {
    this.coursePlanProvider = coursePlanProvider
  }
}
