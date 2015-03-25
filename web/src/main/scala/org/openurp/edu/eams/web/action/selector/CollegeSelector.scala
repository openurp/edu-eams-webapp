package org.openurp.edu.eams.web.action.selector

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.edu.eams.core.service.DepartmentService
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction



class CollegeSelector extends RestrictionSupportAction {

  var departmentService: DepartmentService = _

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
}
