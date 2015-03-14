package org.openurp.edu.eams.teach.election.service.context

import org.beangle.commons.text.i18n.Message
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.teach.lesson.Lesson
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class ElectMessage(key: String, 
    @BeanProperty var `type`: ElectRuleType, 
    @BooleanBeanProperty var success: Boolean, 
    lesson: Lesson) extends Message(key) {

  @BeanProperty
  var lesson: Lesson = _

  if (null != lesson) {
    this.lesson = lesson
  }
}
