package org.openurp.edu.eams.teach.planaudit.service.observers

import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext

import scala.collection.JavaConversions._

trait PlanAuditObserver {

  def notifyStart(): Unit

  def notifyBegin(context: PlanAuditContext, index: Int): Boolean

  def notifyEnd(context: PlanAuditContext, index: Int): Unit

  def finish(): Unit
}
