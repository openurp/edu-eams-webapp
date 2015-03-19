package org.openurp.edu.eams.teach.election.web.action

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class StdElectLogAction extends SemesterSupportAction {

  protected def getEntityName(): String = classOf[ElectLogger].getName

  protected override def indexSetting() {
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val student = getLoginStudent
    val builder = OqlBuilder.from(classOf[ElectLogger], "electLogger")
    builder.where("electLogger.project = :project", student.getProject)
    builder.where("electLogger.stdCode = :stdCode", student.getCode)
    populateConditions(builder)
    builder.limit(getPageLimit)
    val orderBy = get(Order.ORDER_STR)
    builder.orderBy(if (Strings.isEmpty(orderBy)) "electLogger.createdAt desc" else orderBy)
    builder
  }
}
