package org.openurp.edu.eams.web.action.api.internal



import java.util.TreeMap



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
