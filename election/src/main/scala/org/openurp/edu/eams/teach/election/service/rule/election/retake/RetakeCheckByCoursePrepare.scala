package org.openurp.edu.eams.teach.election.service.rule.election.retake

import org.beangle.ems.rule.Context
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import RetakeCheckByCoursePrepare._



object RetakeCheckByCoursePrepare {

  val STATE_PARAM = "RETAKE_UNCHECK_CREDITS"
}

class RetakeCheckByCoursePrepare extends AbstractElectRuleExecutor with ElectRulePrepare {

  def execute(context: Context): Boolean = true

  def prepare(electContext: PrepareContext) {
    electContext.getState.getParams.put(STATE_PARAM, true)
  }
}
