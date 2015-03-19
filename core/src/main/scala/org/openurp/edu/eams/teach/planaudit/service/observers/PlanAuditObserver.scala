package org.openurp.edu.eams.teach.planaudit.service.observers

import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext



trait PlanAuditObserver {

  def notifyStart(): Unit

  def notifyBegin(context: PlanAuditContext, index: Int): Boolean

  def notifyEnd(context: PlanAuditContext, index: Int): Unit

  def finish(): Unit
}
