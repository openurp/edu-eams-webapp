package org.openurp.edu.eams.core.service.event

import org.beangle.commons.event.BusinessEvent
import org.openurp.edu.base.Teacher

import scala.collection.JavaConversions._

@SerialVersionUID(-2271597733343080258L)
class TeacherCreationEvent(source: Teacher) extends BusinessEvent(source)
