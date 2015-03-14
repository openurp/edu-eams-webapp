package org.openurp.edu.eams.teach.web.action.selector

import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.code.StdLabel
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

class CourseSelector extends BaseAction {

  def search(): String = {
    val pageNo = get("pageNo")
    val query = OqlBuilder.from(classOf[Course], "course")
    populateConditions(query)
    if (Strings.isEmpty(pageNo)) {
      put("courseList", entityDao.search(query))
    } else {
      query.limit(getPageLimit)
      put("courseList", entityDao.search(query))
    }
    put("stdTypeList", baseCodeService.getCodes(classOf[StdLabel]))
    forward("list")
  }
}
