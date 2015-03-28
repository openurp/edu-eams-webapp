package org.beangle.commons.dao.impl

import org.beangle.data.model.dao.EntityDao
import org.beangle.commons.event.EventMulticaster
import org.beangle.commons.event.Event

class BaseServiceImpl {

  var entityDao: EntityDao = _

  var eventMulticaster: EventMulticaster = _

  def publish(e: Event) {
    if (null != eventMulticaster) eventMulticaster.multicast(e);
  }

}