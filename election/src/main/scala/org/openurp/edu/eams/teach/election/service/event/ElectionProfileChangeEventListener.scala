package org.openurp.edu.eams.teach.election.service.event

import org.beangle.commons.event.Event
import org.beangle.commons.event.EventListener
import org.openurp.edu.eams.teach.election.service.cache.ProfileLessonDataProvider



class ElectionProfileChangeEventListener extends EventListener[ElectionProfileChangeEvent] {

  def onEvent(event: ElectionProfileChangeEvent) {
    val provider = event.getSource.asInstanceOf[ProfileLessonDataProvider]
    provider.notifyThread()
  }

  def supportsEventType(eventType: Class[_ <: Event]): Boolean = {
    classOf[ElectionProfileChangeEvent].isAssignableFrom(eventType)
  }

  def supportsSourceType(sourceType: Class[_]): Boolean = true
}
