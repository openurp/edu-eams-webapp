package org.openurp.edu.eams.teach.lesson.service.limit.impl

import net.sf.ehcache.store.chm.ConcurrentHashMap
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(53685695269396170L)
class CourseLimitContentContext extends ConcurrentHashMap[Any, Any] {

  @BeanProperty
  var content: String = _

  def get[T](key: AnyRef, `type`: Class[T]): T = get(key).asInstanceOf[T]
}
