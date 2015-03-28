package org.openurp.edu.eams.teach.election.service.rule.election



import org.beangle.commons.collection.Collections
import org.beangle.ems.rule.Context
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.model.constraint.StdCourseCountConstraint
import org.openurp.edu.eams.teach.election.service.CourseTakeService
import org.openurp.edu.eams.teach.election.service.CreditConstraintService
import org.openurp.edu.eams.teach.election.service.context.CourseCountConstraintWrapper
import org.openurp.edu.eams.teach.election.service.context.ElectConstraintWrapper
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan



class CourseCountConstraintChecker extends AbstractElectRuleExecutor with ElectRulePrepare {

  protected var creditConstraintService: CreditConstraintService = _

  protected var courseTakeService: CourseTakeService = _

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    val lesson = electContext.getLesson
    var constraint = electContext.getState.getCourseCountConstraint
    if (null != constraint) {
      if (constraint.isOverMax(1)) {
        electContext.addMessage(new ElectMessage("本学期选课总门数限制已达上限," + constraint.toString, ElectRuleType.ELECTION, 
          false, lesson))
        return false
      }
      val `type` = lesson.getCourseType
      constraint = electContext.getState.getCourseCountConstraint(`type`)
      if (null != constraint && constraint.isOverMax(1)) {
        electContext.addMessage(new ElectMessage("本学期 " + `type`.getName + " 门数限制已达上限 " + constraint.toString, 
          ElectRuleType.ELECTION, false, lesson))
        return false
      }
    }
    true
  }

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.CONSTRAINT_COURSE_COUNT)) {
      val state = context.getState
      val semester = state.getProfile(entityDao).getSemester
      val std = context.getStudent
      val constraint = creditConstraintService.getCourseCountConstraint(semester, std)
      if (null != constraint) {
        val maxCourseCounts = constraint.getCourseTypeMaxCourseCount
        val courseTakes = context.getTakes
        var electedCount = 0
        val plan = context.getPlan
        val electedCourseTypeCourseCounts = Collections.newMap[Any]
        val groups = plan.getGroups
        for (courseGroup <- groups if null != maxCourseCounts.get(courseGroup.getCourseType)) {
          electedCourseTypeCourseCounts.put(courseGroup.getCourseType, new CourseCountConstraintWrapper(maxCourseCounts.get(courseGroup.getCourseType), 
            0))
        }
        for (courseTake <- courseTakes if !courseTake.getCourseTakeType.isRetake) {
          electedCount += 1
          var `type` = courseTake.getLesson.getCourse.getCourseType
          var constraintWrapper = electedCourseTypeCourseCounts.get(`type`)
          if (null == constraintWrapper) {
            `type` = courseTake.getLesson.getCourseType
            constraintWrapper = electedCourseTypeCourseCounts.get(`type`)
          }
          if (null != constraintWrapper) {
            constraintWrapper.addElectedItem(1)
          }
        }
        state.setCourseCountConstraint(constraint, electedCount, electedCourseTypeCourseCounts)
      }
      context.addPreparedDataName(PreparedDataName.CONSTRAINT_COURSE_COUNT)
    }
  }

  def setCreditConstraintService(creditConstraintService: CreditConstraintService) {
    this.creditConstraintService = creditConstraintService
  }

  def setCourseTakeService(courseTakeService: CourseTakeService) {
    this.courseTakeService = courseTakeService
  }
}
