package org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy

import org.hibernate.Query
import org.hibernate.Session
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy

abstract class AbstractLessonFilterStrategy extends LessonFilterStrategy() {

  var name: String = _

  var prefix: String = "from Lesson as lesson "

  var postfix: String = _

  protected def this(name: String) {
    this()
    this.name = name
  }

  def filterString: String

  def createQuery(session: Session): Query = createQuery(session, null, null)

  def queryString: String = queryString(null, null)

  def createQuery(session: Session, prefix: String, postfix: String): Query = {
    val nprefix = if ((null == prefix)) this.prefix else prefix
    val npostfix = if ((null == postfix)) this.postfix else postfix
    session.createQuery(nprefix + filterString + npostfix)
  }

  def queryString(prefix: String, postfix: String): String = {
    val nprefix = if ((null == prefix)) this.prefix else prefix
    val npostfix = if ((null == postfix)) this.postfix else postfix
    nprefix + filterString + npostfix
  }
}
