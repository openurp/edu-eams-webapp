package org.openurp.edu.eams.teach.election.web.action.constraint

import org.beangle.commons.collection.Order
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.action.ActionSupport
import org.beangle.struts2.helper.ContextHelper
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.eams.teach.election.model.Enum.ConstraintType
import org.openurp.edu.eams.teach.election.model.constraint.ConstraintLogger



class ConstraintLoggerAction extends ActionSupport {

  var entityDao: EntityDao = _

  def index(): String = {
    ContextHelper.put("constraintTypes", ConstraintType.values)
    forward()
  }

  def search(): String = {
    val builder = OqlBuilder.from(classOf[ConstraintLogger], "logger")
    QueryHelper.populateConditions(builder)
    val consType = Params.get("fake.constraintType")
    if (Strings.isNotEmpty(consType)) {
      builder.where("str(logger.constraintType) = :constraintType", consType)
    }
    builder.limit(QueryHelper.getPageLimit)
    builder.orderBy(Params.get(Order.ORDER_STR))
    ContextHelper.put("loggers", entityDao.search(builder))
    forward()
  }
}
