package org.openurp.edu.eams.system.report.web.action

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.openurp.edu.eams.system.report.ReportTemplate
import org.openurp.edu.eams.web.action.common.ProjectSupportAction



class ReportTemplateAction extends ProjectSupportAction {

  protected override def getEntityName(): String = classOf[ReportTemplate].getName

  protected override def saveAndForward(entity: Entity[_]): String = {
    val template = entity.asInstanceOf[ReportTemplate]
    if (null == template.getProject) template.setProject(getProject)
    super.saveAndForward(entity)
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = OqlBuilder.from(getEntityName, getShortName)
    populateConditions(builder)
    builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    builder.where("reportTemplate.project=:project", getProject)
    builder
  }
}
