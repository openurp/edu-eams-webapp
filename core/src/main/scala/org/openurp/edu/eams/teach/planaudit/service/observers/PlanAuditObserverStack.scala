package org.openurp.edu.eams.teach.planaudit.service.observers

import java.util.Observable
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.beangle.commons.collection.Collections

class PlanAuditObserverStack(initObersers: PlanAuditObserver*) extends Observable() with PlanAuditObserver {

  for (ob <- initObersers) {
    addObserver(ob)
  }

  var observers = Collections.newBuffer[PlanAuditObserver]

  def addObserver(observer: PlanAuditObserver) {
    observers += (observer)
  }

  def notifyStart() {
    var iterator = observers.iterator
    while (iterator.hasNext) {
      val observer = iterator.next()
      observer.notifyStart()
    }
  }

  def notifyBegin(context: PlanAuditContext, index: Int): Boolean = {
    var canAudit = true
    var iterator = observers.iterator
    while (iterator.hasNext) {
      val observer = iterator.next()
      canAudit &= observer.notifyBegin(context, index)
    }
    canAudit
  }

  def notifyEnd(context: PlanAuditContext, index: Int) {
    var iterator = observers.iterator
    while (iterator.hasNext) {
      val observer = iterator.next()
      observer.notifyEnd(context, index)
    }
  }

  def finish() {
    var iterator = observers.iterator
    while (iterator.hasNext) {
      val observer = iterator.next()
      observer.finish()
    }
  }
}
