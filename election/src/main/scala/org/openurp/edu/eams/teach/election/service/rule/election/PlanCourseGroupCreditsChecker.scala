package org.openurp.edu.eams.teach.election.service.rule.election

import org.beangle.ems.rule.Context
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonNoRetakeFilter
import org.openurp.edu.eams.teach.election.service.rule.election.retake.RetakeCheckByCoursePrepare
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class PlanCourseGroupCreditsChecker extends AbstractElectRuleExecutor with ElectRulePrepare {

  protected var planCreditLimitPrepare: PlanCreditLimitPrepare = _

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    val lesson = electContext.getLesson
    val state = electContext.getState
    val noRetake = true == state.getParams.get(ElectableLessonNoRetakeFilter.PARAM)
    val unCheckCredits = true == state.getParams.get(RetakeCheckByCoursePrepare.STATE_PARAM)
    if (noRetake || !unCheckCredits) {
      if (electContext.getState.getCoursePlan.isOverMaxCredit(lesson)) {
        context.addMessage(new ElectMessage(lesson.getCourseType.getName + " 学分已达上限", ElectRuleType.ELECTION, 
          false, lesson))
        return false
      }
    }
    true
  }

  def prepare(context: PrepareContext) {
    planCreditLimitPrepare.prepare(context)
  }

  def setPlanCreditLimitPrepare(planCreditLimitPrepare: PlanCreditLimitPrepare) {
    this.planCreditLimitPrepare = planCreditLimitPrepare
  }
}
