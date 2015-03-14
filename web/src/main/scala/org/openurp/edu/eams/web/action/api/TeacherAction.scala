package org.openurp.edu.eams.web.action.api

import java.util.Date
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

class TeacherAction extends BaseAction {

  def json(): String = {
    val codeOrName = get("term")
    val query = OqlBuilder.from(classOf[Teacher], "teacher")
    populateConditions(query)
    val teacherDepartId = getInt("teacher.department.id")
    if (teacherDepartId != null) {
      query.where("teacher.department.id = :departmentId", teacherDepartId)
    }
    if (Strings.isNotEmpty(codeOrName)) {
      query.where("(teacher.name like :name or teacher.code like :code)", '%' + codeOrName + '%', '%' + codeOrName + '%')
    }
    val now = new Date()
    query.where(":now1 >= teacher.effectiveAt and (teacher.invalidAt is null or :now2 <= teacher.invalidAt)", 
      now, now)
      .where("teacher.teaching = :teaching", true)
      .orderBy("teacher.name")
      .orderBy("teacher.code")
    val pageLimit = getPageLimit
    query.limit(pageLimit)
    query.orderBy("teacher.name")
    put("teachers", entityDao.search(query))
    put("pageLimit", pageLimit)
    forward()
  }
}
