package org.openurp.edu.eams.teach.lesson.service.limit.impl

import net.sf.ehcache.store.chm.ConcurrentHashMap




@SerialVersionUID(53685695269396170L)
class LessonLimitContentContext extends ConcurrentHashMap[Any, Any] {

  
  var content: String = _

  def get[T](key: AnyRef, `type`: Class[T]): T = get(key).asInstanceOf[T]
}
