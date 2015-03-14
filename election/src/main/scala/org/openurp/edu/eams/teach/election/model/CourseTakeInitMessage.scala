package org.openurp.edu.eams.teach.election.model

import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.util.OutputMessage

import scala.collection.JavaConversions._

class CourseTakeInitMessage(key: String, var lesson: Lesson) extends OutputMessage {

  this.key = key

  def this(key: String, lesson: Lesson, message: String) {
    this()
    this.key = key
    this.lesson = lesson
    this.message = message
  }

  def getMessage(textResource: TextResource): String = {
    val sb = new StringBuilder()
    sb.append(textResource.getText(key))
    sb.append("[").append(lesson.getCourse.getName).append(":")
      .append(lesson.getNo)
      .append("]")
      .append(lesson.getTeachClass.getName)
      .append(":")
    if (Strings.isNotEmpty(getMessage)) sb.append(getMessage)
    sb.toString
  }
}
