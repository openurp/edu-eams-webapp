package org.openurp.edu.eams.teach.election.service.rule

import org.openurp.edu.eams.teach.election.service.context.PrepareContext

import scala.collection.JavaConversions._

trait ElectRulePrepare {

  def prepare(context: PrepareContext): Unit
}
