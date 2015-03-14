package org.openurp.edu.eams.web.action.api.internal

import java.util.Map

import scala.collection.JavaConversions._

trait LessonGsonBuilderWorker {

  def dirtywork(entity: AnyRef, groups: Map[String, Any]): Unit
}
