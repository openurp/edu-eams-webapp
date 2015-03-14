package org.openurp.edu.eams.web.action.selector

import java.util.List
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Major
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

class MajorSelector extends BaseAction {

  def withDepartment(): String = {
    val query = OqlBuilder.from(classOf[Major], "major")
    val departmentId = getInt("departmentId")
    if (departmentId != null) {
      query.where("exists(from major.journals md where md.depart.id = :departmentId)", departmentId)
    }
    query.where("major.enabled = true")
    val specialitList = entityDao.search(query).asInstanceOf[List[_]]
    put("specialitList", specialitList)
    forwardError("success")
  }

  def withoutDepartment(): String = {
    put("specialitList", entityDao.getAll(classOf[Major]))
    forwardError("success")
  }
}
