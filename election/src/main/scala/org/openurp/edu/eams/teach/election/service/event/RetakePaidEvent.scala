package org.openurp.edu.eams.teach.election.service.event

import org.beangle.commons.event.BusinessEvent

import scala.collection.JavaConversions._

@SerialVersionUID(3074377793517695646L)
class RetakePaidEvent(source: AnyRef) extends BusinessEvent(source)
