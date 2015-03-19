package org.openurp.edu.eams.teach.web.action.selector

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.code.StdLabel
import org.openurp.edu.base.Course
import org.openurp.edu.eams.web.action.BaseAction



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
