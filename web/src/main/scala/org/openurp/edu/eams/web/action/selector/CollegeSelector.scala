package org.openurp.edu.eams.web.action.selector

import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.edu.eams.core.service.DepartmentService
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction

import scala.collection.JavaConversions._

class CollegeSelector extends RestrictionSupportAction {

  private var departmentService: DepartmentService = _

  def withAuthority(): String = {
    put("depertmentSet", baseInfoService.getBaseInfos(classOf[Department]))
    "success"
  }

  def withoutAuthority(): String = {
    put("depertmentSet", departmentService.getColleges)
    "success"
  }

  def search(): String = {
    val builder = OqlBuilder.from(classOf[Department], "department").where("department.college=true")
    populateConditions(builder)
    if (Strings.isNotEmpty(get("pageNo"))) {
      builder.limit(getPageLimit)
    }
    put("departmentList", entityDao.search(builder))
    "list"
  }

  def setDepartmentService(departmentService: DepartmentService) {
    this.departmentService = departmentService
  }
}
