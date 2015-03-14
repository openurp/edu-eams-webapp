package org.openurp.edu.eams.teach.lesson.service

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.event.BusinessEvent

import scala.collection.JavaConversions._

class LessonLogHelper extends BaseServiceImpl {

  def log(detail: String) {
    val logEvent = new BusinessEvent(1)
    logEvent.setDetail(detail)
    logEvent.setResource("教学任务")
    publish(logEvent)
  }
}
