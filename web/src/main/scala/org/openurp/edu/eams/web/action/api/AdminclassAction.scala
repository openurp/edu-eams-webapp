package org.openurp.edu.eams.web.action.api

import java.util.Date
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.web.action.common.ProjectSupportAction



class AdminclassAction extends ProjectSupportAction {

  def json(): String = {
    var codeOrNames = get("term")
    val query = OqlBuilder.from(classOf[Adminclass], "adminclass")
    populateConditions(query, "adminclass.grade")
    query.where("adminclass.major.project.id = :projectId", getProject.id)
    val grade = get("adminclass.grade")
    if (Strings.isNotEmpty(grade)) {
      query.where("adminclass.grade in (:grades)", Strings.split(grade))
    }
    if (Strings.isNotEmpty(codeOrNames)) {
      codeOrNames = codeOrNames.replace('，', ',').replaceAll(",+", ",")
      val conds = Strings.split(codeOrNames)
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
          sb.append("adminclass.name like ").append(like).append(" or adminclass.code like ")
            .append(like)
        }
        sb.append("\n)")
        query.where(sb.toString)
      }
    }
    val grade_ = get("grade")
    if (Strings.isNotBlank(grade_) && grade_ != "null") {
      query.where("adminclass.grade in (:grades)", Strings.split(grade_))
    }
    val educationStr = get("educations")
    if (Strings.isNotBlank(educationStr) && educationStr != "null") {
      val educations = Strings.splitToInt(educationStr)
      query.where("adminclass.education.id in (:educations)", educations)
    }
    val departStr = get("departs")
    if (Strings.isNotBlank(departStr) && departStr != "null") {
      val departs = Strings.splitToInt(departStr)
      query.where("adminclass.department.id in (:departs)", departs)
    }
    val majorStr = get("majors")
    if (Strings.isNotBlank(majorStr) && majorStr != "null") {
      val majors = Strings.splitToInt(majorStr)
      query.where("adminclass.major.id in (:majors)", majors)
    }
    val directionStr = get("directions")
    if (Strings.isNotBlank(directionStr) && directionStr != "null") {
      val directions = Strings.splitToInt(directionStr)
      query.where("adminclass.direction.id in (:directions)", directions)
    }
    val stdTypeStr = get("stdTypes")
    if (Strings.isNotBlank(stdTypeStr) && stdTypeStr != "null") {
      val stdTypes = Strings.splitToInt(stdTypeStr)
      query.where("adminclass.stdType.id in (:stdTypes)", stdTypes)
    }
    val now = new Date()
    query.where(":now1 >= adminclass.effectiveAt and (adminclass.invalidAt is null or :now2 <= adminclass.invalidAt)", 
      now, now)
      .orderBy("adminclass.code")
    query.orderBy("adminclass.code")
    put("adminclasses", entityDao.search(query))
    forward()
  }
}
