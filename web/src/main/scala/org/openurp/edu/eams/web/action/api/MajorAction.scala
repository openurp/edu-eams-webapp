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
import org.openurp.edu.base.Major
import org.openurp.edu.base.MajorJournal
import org.openurp.edu.eams.web.action.api.internal.LessonGsonBuilderHelper
import org.openurp.edu.eams.web.action.api.internal.LessonGsonBuilderWorker
import org.openurp.edu.eams.web.action.common.ProjectSupportAction
import com.google.gson.Gson

import scala.collection.JavaConversions._

class MajorAction extends ProjectSupportAction {

  def json(): String = {
    val educationIds = Strings.splitToInt(get("educationIds"))
    val departmentIds = Strings.splitToInt(get("departmentIds"))
    var warnings = ""
    if (Arrays.isEmpty(educationIds)) {
      warnings += "请先选择学历层次\n"
    }
    if (Arrays.isEmpty(departmentIds)) {
      warnings += "请先选择上课院系"
    }
    var majors = new ArrayList[Major]()
    if (Strings.isBlank(warnings)) {
      val query = OqlBuilder.from(classOf[Major], "major")
      query.where("major.project.id = :projectId", getProject.getId)
        .where("exists(from major.educations e where e.id in (:educationIds))", educationIds)
        .where("exists(from major.journals md where md.depart.id in (:departIds))", departmentIds)
        .orderBy("major.code, major.name")
      majors = entityDao.search(query)
    }
    val gson = new Gson()
    val json = gson.toJson(LessonGsonBuilderHelper.genGroupResult(majors, warnings, new LessonGsonBuilderWorker() {

      def dirtywork(`object`: AnyRef, groups: Map[String, Any]) {
        var major = `object`.asInstanceOf[Major]
        for (md <- major.getJournals) {
          var groupName = md.getDepart.getName.toString
          if (groups.get(groupName) == null) {
            groups.put(groupName, new ArrayList[Map[String, Any]]())
          }
          var entities = groups.get(groupName).asInstanceOf[List[Map[String, Any]]]
          var entity = new TreeMap[String, Any]()
          entity.put("id", major.getId)
          entity.put("name", major.getName)
          entity.put("code", major.getCode)
          if (!entities.contains(entity)) {
            entities.add(entity)
          }
        }
      }
    }))
    val response = getResponse
    response.setContentType("text/plain;charset=UTF-8")
    response.getWriter.write(json)
    response.getWriter.close()
    null
  }
}
