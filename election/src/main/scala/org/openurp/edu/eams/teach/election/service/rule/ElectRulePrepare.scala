package org.openurp.edu.eams.teach.election.service.rule

import org.openurp.edu.eams.teach.election.service.context.PrepareContext



trait ElectRulePrepare {

  def prepare(context: PrepareContext): Unit
}
