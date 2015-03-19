package org.openurp.edu.eams.web.helper

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Teacher



class AdminclassSearchHelper extends SearchHelper {

  def buildQuery(teacher: Teacher): OqlBuilder[Adminclass] = {
    val builder = OqlBuilder.from(classOf[Adminclass], "adminclass")
    QueryHelper.populateConditions(builder)
    builder.where("exists(from adminclass.instructors instructor where instructor=:teacher) " + 
      "or exists(from adminclass.tutors tutor where tutor=:teacher)", teacher)
    builder.where("adminclass.effectiveAt <= :now and (adminclass.invalidAt is null or adminclass.invalidAt >= :now)", 
      new java.util.Date())
    builder.select("select distinct adminclass")
    builder.limit(QueryHelper.getPageLimit)
    val order = Params.get(Order.ORDER_STR)
    builder.orderBy(if (Strings.isEmpty(order)) "adminclass.grade desc" else order)
    builder
  }
}
