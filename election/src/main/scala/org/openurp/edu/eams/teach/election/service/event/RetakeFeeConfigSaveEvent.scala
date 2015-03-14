package org.openurp.edu.eams.teach.election.service.event

import org.beangle.commons.event.BusinessEvent
import org.openurp.edu.eams.teach.election.RetakeFeeConfig

import scala.collection.JavaConversions._

@SerialVersionUID(3074377793517695646L)
class RetakeFeeConfigSaveEvent(source: RetakeFeeConfig) extends BusinessEvent(source) {

  override def getSource(): RetakeFeeConfig = source.asInstanceOf[RetakeFeeConfig]
}
