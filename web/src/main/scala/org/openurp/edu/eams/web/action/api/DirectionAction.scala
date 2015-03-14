package org.openurp.edu.eams.web.action.api

import java.io.IOException
import java.util.ArrayList
import java.util.List
import java.util.Map
import java.util.TreeMap
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Direction
import org.openurp.edu.eams.web.action.api.internal.LessonGsonBuilderHelper
import org.openurp.edu.eams.web.action.api.internal.LessonGsonBuilderWorker
import org.openurp.edu.eams.web.action.common.ProjectSupportAction
import com.google.gson.Gson

import scala.collection.JavaConversions._

class DirectionAction extends ProjectSupportAction {

  def json(): String = {
    val majorIds = Strings.splitToInt(get("majorIds"))
    val departmentIds = Strings.splitToInt(get("departmentIds"))
    var warnings = ""
    if (Arrays.isEmpty(majorIds)) {
      warnings += "请先选择专业"
    }
    if (Strings.isNotBlank(warnings)) {
      put("warnings", warnings)
      return forward("directionsJSON")
    }
    var directions = new ArrayList[Direction]()
    if (Strings.isBlank(warnings)) {
      val query = OqlBuilder.from(classOf[Direction], "direction")
      query.where("direction.major.project.id = :projectId", getSession.get("projectId").asInstanceOf[java.lang.Integer])
        .where("direction.major.id in (:majorIds)", majorIds)
        .orderBy("direction.code, direction.name")
      if (null != departmentIds && departmentIds.length > 0) {
        query.where("exists(from direction.departs dd where dd.depart.id in (:departIds))", departmentIds)
      }
      directions = entityDao.search(query)
    }
    val gson = new Gson()
    val json = gson.toJson(LessonGsonBuilderHelper.genGroupResult(directions, warnings, new LessonGsonBuilderWorker() {

      def dirtywork(`object`: AnyRef, groups: Map[String, Any]) {
        var rawEntity = `object`.asInstanceOf[Direction]
        var groupName = rawEntity.major.getName
        if (groups.get(groupName) == null) {
          groups.put(groupName, new ArrayList[Map[String, Any]]())
        }
        var entities = groups.get(groupName).asInstanceOf[List[Map[String, Any]]]
        var entity = new TreeMap[String, Any]()
        entity.put("id", rawEntity.getId)
        entity.put("name", rawEntity.getName)
        entity.put("code", rawEntity.getCode)
        entities.add(entity)
      }
    }))
    val response = getResponse
    response.setContentType("text/plain;charset=UTF-8")
    response.getWriter.write(json)
    response.getWriter.close()
    null
  }
}
