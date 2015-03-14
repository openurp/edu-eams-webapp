package org.openurp.edu.eams.teach.program.common.helper

import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorPlanBean
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseBean
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseGroupBean
import org.openurp.edu.eams.teach.program.original.OriginalPlan
import org.openurp.edu.eams.teach.program.original.OriginalPlanCourse
import org.openurp.edu.eams.teach.program.original.OriginalPlanCourseGroup
import org.openurp.edu.eams.teach.program.original.model.OriginalPlanBean
import org.openurp.edu.eams.teach.program.original.model.OriginalPlanCourseBean
import org.openurp.edu.eams.teach.program.original.model.OriginalPlanCourseGroupBean
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlanCourse
import org.openurp.edu.eams.teach.program.personal.PersonalPlanCourseGroup
import org.openurp.edu.eams.teach.program.personal.model.PersonalPlanBean
import org.openurp.edu.eams.teach.program.personal.model.PersonalPlanCourseBean
import org.openurp.edu.eams.teach.program.personal.model.PersonalPlanCourseGroupBean
//remove if not needed
import scala.collection.JavaConversions._

object ProgramHibernateClassGetter {

  def hibernateClass(planGroup: CourseGroup): Class[_ <: CourseGroup] = {
    if (classOf[OriginalPlanCourseGroupBean].isAssignableFrom(planGroup.getClass)) {
      return classOf[OriginalPlanCourseGroup]
    }
    if (classOf[PersonalPlanCourseGroupBean].isAssignableFrom(planGroup.getClass)) {
      return classOf[PersonalPlanCourseGroup]
    }
    if (classOf[MajorPlanCourseGroupBean].isAssignableFrom(planGroup.getClass)) {
      return classOf[MajorPlanCourseGroup]
    }
    null
  }

  def hibernateClass(planCourse: PlanCourse): Class[_ <: PlanCourse] = {
    if (classOf[OriginalPlanCourseBean].isAssignableFrom(planCourse.getClass)) {
      return classOf[OriginalPlanCourse]
    }
    if (classOf[PersonalPlanCourseBean].isAssignableFrom(planCourse.getClass)) {
      return classOf[PersonalPlanCourse]
    }
    if (classOf[MajorPlanCourseBean].isAssignableFrom(planCourse.getClass)) {
      return classOf[MajorPlanCourse]
    }
    null
  }

  def hibernateClass(plan: CoursePlan): Class[_ <: CoursePlan] = {
    if (classOf[OriginalPlanBean].isAssignableFrom(plan.getClass)) {
      return classOf[OriginalPlan]
    }
    if (classOf[PersonalPlanBean].isAssignableFrom(plan.getClass)) {
      return classOf[PersonalPlan]
    }
    if (classOf[MajorPlanBean].isAssignableFrom(plan.getClass)) {
      return classOf[MajorPlan]
    }
    null
  }
}
