package org.openurp.edu.eams.teach.schedule.service

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.event.BusinessEvent

import scala.collection.JavaConversions._

class ScheduleLogHelper extends BaseServiceImpl {

  def log(logDetails: Array[String]) {
    val logEvent = new BusinessEvent(1)
    logEvent.setDetail(logDetails(0))
    logEvent.setSubject(logDetails(1))
    logEvent.setResource("排课日志")
    publish(logEvent)
  }
}
