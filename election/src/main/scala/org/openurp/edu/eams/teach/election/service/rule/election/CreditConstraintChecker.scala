package org.openurp.edu.eams.teach.election.service.rule.election

import org.beangle.ems.rule.Context
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.model.constraint.AbstractCreditConstraint
import org.openurp.edu.eams.teach.election.service.CreditConstraintService
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



class CreditConstraintChecker extends AbstractElectRuleExecutor with ElectRulePrepare {

  protected var creditConstraintService: CreditConstraintService = _

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    val lesson = electContext.getLesson
    val constraint = electContext.getState.getCreditConstraint
    if (null != constraint) {
      if (constraint.isOverMax(lesson.getCourse.getCredits)) {
        electContext.addMessage(new ElectMessage("本学期选课学分限制已达上限," + constraint.toString, ElectRuleType.ELECTION, 
          false, lesson))
        return false
      }
    }
    true
  }

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.CONSTRAINT_MAX_CREDIT)) {
      val state = context.getState
      var constraint = creditConstraintService.getCreditConstraint(state.getProfile(entityDao).getSemester, 
        context.getStudent)
      var electedCredits = 0f
      for (courseTake <- context.getTakes) {
        electedCredits += courseTake.getLesson.getCourse.getCredits
      }
      val originalMax = constraint.getMaxCredit
      constraint = doSomethingWithConstraint(context, constraint)
      state.setCreditConstraint(constraint, electedCredits)
      constraint.setMaxCredit(originalMax)
      context.addPreparedDataName(PreparedDataName.CONSTRAINT_MAX_CREDIT)
    }
  }

  protected def doSomethingWithConstraint(context: PrepareContext, constraint: AbstractCreditConstraint): AbstractCreditConstraint = {
    constraint
  }

  def setCreditConstraintService(creditConstraintService: CreditConstraintService) {
    this.creditConstraintService = creditConstraintService
  }
}
