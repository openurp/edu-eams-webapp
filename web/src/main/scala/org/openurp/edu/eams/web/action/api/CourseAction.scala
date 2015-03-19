package org.openurp.edu.eams.web.action.api

import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Course
import org.openurp.edu.eams.web.action.BaseAction



class CourseAction extends BaseAction {

  def json(): String = {
    val codeOrName = get("term")
    val query = OqlBuilder.from(classOf[Course], "course")
    populateConditions(query)
    if (Strings.isNotEmpty(codeOrName)) {
      query.where("(course.name like :name or course.code like :code)", '%' + codeOrName + '%', '%' + codeOrName + '%')
    }
    val pageLimit = getPageLimit
    query.limit(pageLimit)
    query.orderBy("course.name")
    put("courses", entityDao.search(query))
    put("pageLimit", pageLimit)
    forward()
  }
}
