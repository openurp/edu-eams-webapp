package org.openurp.edu.eams.teach.web.action.selector

import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.web.action.BaseAction



class CourseTypeSelector extends BaseAction {

  def withAuthority(): String = forwardError("success")

  def withoutAuthority(): String = {
    put("courseTypeList", entityDao.getAll(classOf[CourseType]))
    forward("res")
  }
}
