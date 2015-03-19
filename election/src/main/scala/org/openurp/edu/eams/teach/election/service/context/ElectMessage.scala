package org.openurp.edu.eams.teach.election.service.context

import org.beangle.commons.text.i18n.Message
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.teach.lesson.Lesson




class ElectMessage(key: String, 
     var `type`: ElectRuleType, 
     var success: Boolean, 
    lesson: Lesson) extends Message(key) {

  
  var lesson: Lesson = _

  if (null != lesson) {
    this.lesson = lesson
  }
}
