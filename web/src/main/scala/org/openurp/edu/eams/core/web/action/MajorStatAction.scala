package org.openurp.edu.eams.core.web.action


import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Major
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction



class MajorStatAction extends RestrictionSupportAction {

  def index(): String = forward()

  def list(): String = {
    val query = OqlBuilder.from(classOf[Major], "major")
    populateConditions(query)
    query.orderBy(Order.parse(get("orderBy")))
    put("specialities", entityDao.search(query))
    forward()
  }

  def distribution(): String = {
    val query = OqlBuilder.from(classOf[Major], "major")
    populateConditions(query)
    put("specialities", entityDao.search(query))
    forward()
  }

  def structure(): String = {
    val query = OqlBuilder.from(classOf[Major], "major")
    query.select("new  org.openurp.edu.eams.util.stat.StatItem(major.subject.category.id,count(*))")
    populateConditions(query)
    query.groupBy("major.subject.category.id")
    val stats = entityDao.search(query)
    put("stats", stats)
    forward()
  }
}
