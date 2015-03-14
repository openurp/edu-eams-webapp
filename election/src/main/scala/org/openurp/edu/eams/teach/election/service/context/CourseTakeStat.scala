package org.openurp.edu.eams.teach.election.service.context

import org.beangle.commons.lang.Assert
import org.openurp.edu.teach.lesson.Lesson
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class CourseTakeStat[T] {

  @BeanProperty
  var statBy: T = _

  @BeanProperty
  var id: Long = _

  @BeanProperty
  var count: Long = _

  @BeanProperty
  var lesson: Lesson = _

  def this(id: Long, count: Long, statBy: T) {
    this()
    this.id = id
    this.count = count
    this.statBy = statBy
  }

  def setLesson(lesson: Lesson) {
    lesson.getId == this.id
    this.lesson = lesson
  }
}
