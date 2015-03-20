package org.openurp.edu.eams.teach.lesson.service

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.event.BusinessEvent



class LessonLogHelper extends BaseServiceImpl {

  def log(detail: String) {
    val logEvent = new BusinessEvent(1)
    logEvent.detail=detail
    logEvent.resource="教学任务"
    publish(logEvent)
  }
}
