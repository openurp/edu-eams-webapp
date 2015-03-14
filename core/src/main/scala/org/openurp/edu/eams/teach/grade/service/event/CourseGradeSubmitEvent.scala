package org.openurp.edu.eams.teach.grade.service.event

import org.beangle.commons.event.BusinessEvent
import org.openurp.edu.teach.grade.CourseGradeState

import scala.collection.JavaConversions._

@SerialVersionUID(7334716231316808006L)
class CourseGradeSubmitEvent(source: CourseGradeState) extends BusinessEvent(source)
