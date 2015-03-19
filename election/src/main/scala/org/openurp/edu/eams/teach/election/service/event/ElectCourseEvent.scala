package org.openurp.edu.eams.teach.election.service.event

import org.beangle.commons.event.BusinessEvent
import org.openurp.edu.teach.lesson.CourseTake
import ElectCourseEvent._



object ElectCourseEvent {

  def create(source: CourseTake): ElectCourseEvent = new ElectCourseEvent(source)
}

@SerialVersionUID(2721467858671088410L)
class ElectCourseEvent(source: CourseTake) extends BusinessEvent(source) {

  override def getSource(): CourseTake = source.asInstanceOf[CourseTake]
}
