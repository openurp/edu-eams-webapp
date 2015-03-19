package org.openurp.edu.eams.teach.election.service.rule.election


import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.ems.rule.Context
import org.openurp.edu.base.Student
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



class TotalCreditConstraintChecker extends AbstractElectRuleExecutor with ElectRulePrepare {

  protected var creditConstraintService: CreditConstraintService = _

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    val state = electContext.getState
    val lesson = electContext.getLesson
    val constraint = state.getTotalCreditConstraint
    if (constraint != null) {
      if (constraint.isOverMax(lesson.getCourse.getCredits)) {
        electContext.addMessage(new ElectMessage("全程学分限制已达上限", ElectRuleType.ELECTION, false, lesson))
        return false
      }
    }
    true
  }

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.CONSTRAINT_TOTAL_MAX_CREDIT)) {
      val state = context.getState
      val std = context.getStudent
      val constraint = creditConstraintService.getTotalCreditConstraint(std)
      val builder = OqlBuilder.from(classOf[CourseTake].getName + " take")
      builder.select("sum(take.lesson.course.credits)")
      builder.where("take.std=:std", std)
      val it = entityDao.search(builder).iterator()
      var electedCredits: java.lang.Float = null
      if (it.hasNext) {
        val n = it.next()
        if (null != n) {
          electedCredits = n.floatValue()
        }
      }
      state.setCreditConstraint(constraint, electedCredits)
      state.setTotalCreditConstraint(constraint, electedCredits)
      context.addPreparedDataName(PreparedDataName.CONSTRAINT_TOTAL_MAX_CREDIT)
    }
  }

  def setCreditConstraintService(creditConstraintService: CreditConstraintService) {
    this.creditConstraintService = creditConstraintService
  }
}
