package org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy

import org.hibernate.Query
import org.hibernate.Session
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

abstract class AbstractLessonFilterStrategy extends LessonFilterStrategy() {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var prefix: String = "from Lesson as lesson "

  @BeanProperty
  var postfix: String = _

  protected def this(name: String) {
    this()
    this.name = name
  }

  def getFilterString(): String

  def createQuery(session: Session): Query = createQuery(session, null, null)

  def getQueryString(): String = getQueryString(null, null)

  def createQuery(session: Session, prefix: String, postfix: String): Query = {
    prefix = if ((null == prefix)) this.prefix else prefix
    postfix = if ((null == postfix)) this.postfix else postfix
    session.createQuery(prefix + getFilterString + postfix)
  }

  def getQueryString(prefix: String, postfix: String): String = {
    prefix = if ((null == prefix)) this.prefix else prefix
    postfix = if ((null == postfix)) this.postfix else postfix
    prefix + getFilterString + postfix
  }
}
