package org.openurp.edu.eams.web.action.selector

import org.openurp.edu.base.code.StdLabel
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction



class StudentTypeSelector extends RestrictionSupportAction {

  def withAuthority(): String = {
    put("studentTypeList", baseCodeService.getCodes(classOf[StdLabel]))
    "success"
  }

  def withoutAuthority(): String = {
    put("studentTypeList", entityDao.getAll(classOf[StdLabel]))
    "success"
  }
}
