package org.openurp.edu.eams.teach.schedule.service

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.event.BusinessEvent



class ScheduleLogHelper extends BaseServiceImpl {

  def log(logDetails: Array[String]) {
    val logEvent = new BusinessEvent(1)
    logEvent.detail = logDetails(0)
    logEvent.subject = logDetails(1)
    logEvent.resource = "排课日志"
    publish(logEvent)
  }
}
