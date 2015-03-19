package org.openurp.edu.eams.teach.grade.course.web.helper






class StringBuilderHelper {

  def getResponseJSON(map: Map[String, String]): String = {
    if (map.isEmpty) return "{}"
    val strBuilder = new StringBuilder()
    val it = map.keySet.iterator()
    var key = ""
    strBuilder.append("{")
    while (it.hasNext) {
      key = it.next()
      strBuilder.append("\"" + key + "\":\"" + map.get(key) + "\",")
    }
    strBuilder.append("}")
    strBuilder.deleteCharAt(strBuilder.length - 2).toString
  }
}
