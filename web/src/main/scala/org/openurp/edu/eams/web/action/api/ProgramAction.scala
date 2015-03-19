package org.openurp.edu.eams.web.action.api

import java.util.Date


import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Program
import org.openurp.edu.eams.web.action.common.ProjectSupportAction
import com.google.gson.Gson



class ProgramAction extends ProjectSupportAction {

  def json(): String = {
    val query = OqlBuilder.from(classOf[Program], "program")
    populateConditions(query, "program.grade")
    query.where("program.major.project.id = :projectId", getProject.id)
    query.where(":now >= program.effectiveOn and (program.invalidOn is null or :now <= program.invalidOn)", 
      new Date())
      .orderBy("program.name")
    query.limit(getPageLimit)
    var names = get("term")
    if (Strings.isNotEmpty(names)) {
      names = names.replace('，', ',').replaceAll(",+", ",")
      val conds = Strings.split(names)
      if (null != conds && conds.length != 0) {
        val sb = new StringBuilder()
        sb.append("(\n")
        for (i <- 0 until conds.length) {
          val like = "'%" + conds(i) + "%'"
          if (Strings.isEmpty(like)) {
            //continue
          }
          if (i != 0) {
            sb.append("\n or ")
          }
          sb.append("program.name like ").append(like)
        }
        sb.append("\n)")
        query.where(sb.toString)
      }
    }
    val grade = get("grade")
    if (Strings.isNotBlank(grade) && grade != "null") {
      query.where("program.grade in (:grades)", Strings.split(grade))
    }
    val educationStr = get("educations")
    if (Strings.isNotBlank(educationStr) && educationStr != "null") {
      val educations = Strings.splitToInt(educationStr)
      query.where("program.education.id in (:educations)", educations)
    }
    val departStr = get("departs")
    if (Strings.isNotBlank(departStr) && departStr != "null") {
      val departs = Strings.splitToInt(departStr)
      query.where("program.department.id in (:departs)", departs)
    }
    val majorStr = get("majors")
    if (Strings.isNotBlank(majorStr) && majorStr != "null") {
      val majors = Strings.splitToInt(majorStr)
      query.where("program.major.id in (:majors)", majors)
    }
    val directionStr = get("directions")
    if (Strings.isNotBlank(directionStr) && directionStr != "null") {
      val directions = Strings.splitToInt(directionStr)
      query.where("program.direction.id in (:directions)", directions)
    }
    val stdTypeStr = get("stdTypes")
    if (Strings.isNotBlank(stdTypeStr) && stdTypeStr != "null") {
      val stdTypes = Strings.splitToInt(stdTypeStr)
      query.where("program.stdType.id in (:stdTypes)", stdTypes)
    }
    val result = CollectUtils.newArrayList()
    val programs = entityDao.search(query)
    for (program <- programs) {
      val entity = CollectUtils.newHashMap()
      entity.put("id", program.id)
      entity.put("name", program.getName)
      result.add(entity)
    }
    put("programsJSON", new Gson().toJson(result))
    forward()
  }
}
