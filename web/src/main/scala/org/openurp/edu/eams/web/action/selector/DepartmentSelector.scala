package org.openurp.edu.eams.web.action.selector

import org.openurp.base.Department
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction

import scala.collection.JavaConversions._

class DepartmentSelector extends RestrictionSupportAction {

  def withAuthority(): String = {
    put("depertmentSet", baseInfoService.getBaseInfos(classOf[Department]))
    "success"
  }

  def withoutAuthority(): String = {
    put("depertmentSet", entityDao.getAll(classOf[Department]))
    "success"
  }
}
