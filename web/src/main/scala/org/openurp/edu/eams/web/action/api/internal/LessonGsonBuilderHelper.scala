package org.openurp.edu.eams.web.action.api.internal

import java.util.List
import java.util.Map
import java.util.TreeMap

import scala.collection.JavaConversions._

object LessonGsonBuilderHelper {

  def genGroupResult(entities: List[_], warnings: String, worker: LessonGsonBuilderWorker): Map[String, Any] = {
    val groupResult = new TreeMap[String, Any]()
    groupResult.put("warnings", warnings)
    groupResult.put("groups", new TreeMap[String, Any]())
    val groups = groupResult.get("groups").asInstanceOf[TreeMap[String, Any]]
    for (entity <- entities) {
      worker.dirtywork(entity, groups)
    }
    groupResult
  }
}
