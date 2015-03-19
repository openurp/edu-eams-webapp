package org.openurp.edu.eams.teach.election.service.rule.election



import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.election.service.context.ElectCourseGroup
import org.openurp.edu.eams.teach.election.service.context.ElectCoursePlan
import org.openurp.edu.eams.teach.election.service.context.ElectCourseSubstitution
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.teach.plan.CourseSubstitution
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.service.CourseSubstitutionService



class CoursePlanPrepare extends ElectRulePrepare {

  protected var courseSubstitutionService: CourseSubstitutionService = _

  protected var currentTermPrepare: CurrentTermPrepare = _

  def prepare(context: PrepareContext) {
    if (context.isPreparedData(PreparedDataName.COURSE_PLAN)) return
    currentTermPrepare.prepare(context)
    val state = context.getState
    val plan = context.getPlan
    var electPlan = state.getCoursePlan
    if (null == electPlan) {
      electPlan = new ElectCoursePlan()
      context.getState.setCoursePlan(electPlan)
    }
    if (null != plan) {
      val groups = getTopCourseGroups(plan)
      for (group <- groups) {
        addGroup(group, electPlan, null, state)
      }
    }
    val substitutions = courseSubstitutionService.getCourseSubstitutions(context.getStudent)
    for (substitution <- substitutions) {
      var courseTypeId: java.lang.Integer = null
      val electCourseSubstitution = new ElectCourseSubstitution()
      for (course <- substitution.getOrigins) {
        electCourseSubstitution.getOrigins.add(course.id)
        if (null != courseTypeId) {
          //continue
        } else {
          courseTypeId = electPlan.courseIds.get(course.id)
        }
      }
      if (null != courseTypeId) {
        for (course <- substitution.getSubstitutes) {
          electCourseSubstitution.getSubstitutes.add(course.id)
          electPlan.courseIds.put(course.id, courseTypeId)
        }
      } else {
        for (course <- substitution.getSubstitutes) {
          electCourseSubstitution.getSubstitutes.add(course.id)
        }
      }
      state.addCourseSubsititution(electCourseSubstitution)
    }
    context.addPreparedDataName(PreparedDataName.COURSE_PLAN)
  }

  protected def addGroup(group: CourseGroup, 
      electPlan: ElectCoursePlan, 
      parent: ElectCourseGroup, 
      state: ElectState) {
    val electGroup = new ElectCourseGroup(group.getCourseType)
    electGroup.setParent(parent)
    if (null != parent) parent.getChildren.add(electGroup)
    electPlan.addGroup(electGroup)
    for (childGroup <- group.getChildren) {
      addGroup(childGroup, electPlan, electGroup, state)
    }
    for (planCourse <- group.getPlanCourses) {
      electPlan.courseIds.put(planCourse.getCourse.id, group.getCourseType.id)
      electGroup.addCourse(planCourse.getCourse)
    }
  }

  protected def getTopCourseGroups(plan: CoursePlan): List[CourseGroup] = {
    if (plan.getGroups == null) {
      return new ArrayList[CourseGroup]()
    }
    val res = new ArrayList[CourseGroup]()
    for (group <- plan.getGroups if group != null && group.getParent == null) {
      res.add(group.asInstanceOf[CourseGroup])
    }
    res
  }

  def setCourseSubstitutionService(courseSubstitutionService: CourseSubstitutionService) {
    this.courseSubstitutionService = courseSubstitutionService
  }

  def setCurrentTermPrepare(currentTermPrepare: CurrentTermPrepare) {
    this.currentTermPrepare = currentTermPrepare
  }
}
