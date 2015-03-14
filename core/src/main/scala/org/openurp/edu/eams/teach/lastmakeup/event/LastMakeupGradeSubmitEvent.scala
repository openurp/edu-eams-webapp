package org.openurp.edu.eams.teach.lastmakeup.event

import org.beangle.commons.event.BusinessEvent
import org.openurp.edu.eams.teach.lastmakeup.LastMakeupTask

import scala.collection.JavaConversions._

@SerialVersionUID(1419626339604345792L)
class LastMakeupGradeSubmitEvent(task: LastMakeupTask) extends BusinessEvent(task) {

  def getTask(): LastMakeupTask = getSource.asInstanceOf[LastMakeupTask]
}
