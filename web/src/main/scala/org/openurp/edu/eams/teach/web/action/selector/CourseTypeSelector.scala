package org.openurp.edu.eams.teach.web.action.selector

import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

class CourseTypeSelector extends BaseAction {

  def withAuthority(): String = forwardError("success")

  def withoutAuthority(): String = {
    put("courseTypeList", entityDao.getAll(classOf[CourseType]))
    forward("res")
  }
}
