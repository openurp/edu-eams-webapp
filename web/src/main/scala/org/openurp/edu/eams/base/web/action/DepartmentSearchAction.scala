package org.openurp.edu.eams.base.web.action

import java.util.Collection
import java.util.List
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.Page
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.base.Department

import scala.collection.JavaConversions._

class DepartmentSearchAction extends BaseInfoAction {

  def getEntityName(): String = classOf[Department].getName

  def index(): String = forward()

  def search(): String = {
    val departments = entityDao.search(buildDepartmentQuery()).asInstanceOf[Page[Department]]
    put("departments", departments)
    forward()
  }

  protected def buildDepartmentQuery(): OqlBuilder[Department] = {
    val query = OqlBuilder.from(classOf[Department], "department")
    QueryHelper.populateConditions(query)
    query.limit(QueryHelper.getPageLimit)
    val departs = restrictionHelper.getDeparts.asInstanceOf[List[Department]]
    if (departs.isEmpty) {
      query.where("1=2")
    } else {
      query.where("department in (:departs)", departs)
    }
    var orderByPras = Params.get("orderBy")
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "department.code"
    }
    query.orderBy(Order.parse(orderByPras))
    query
  }

  protected def getExportDatas(): Collection[Department] = {
    val query = buildDepartmentQuery()
    query.limit(null)
    entityDao.search(query)
  }

  def info(): String = {
    val departmentId = getIntId("department")
    if (null == departmentId) {
      return forwardError(Array("entity.department", "error.model.id.needed"))
    }
    put("department", entityDao.get(classOf[Department], departmentId))
    forward()
  }
}
