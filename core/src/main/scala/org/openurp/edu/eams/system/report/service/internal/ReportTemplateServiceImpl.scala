package org.openurp.edu.eams.system.report.service.internal

import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Project
import org.openurp.edu.eams.system.report.ReportTemplate
import org.openurp.edu.eams.system.report.service.ReportTemplateService

import scala.collection.JavaConversions._

class ReportTemplateServiceImpl extends BaseServiceImpl with ReportTemplateService {

  def getTemplate(project: Project, code: String): ReportTemplate = {
    val builder = OqlBuilder.from(classOf[ReportTemplate], "rt")
    builder.where("rt.project =:project and rt.code=:code", project, code)
      .cacheable()
    val templates = entityDao.search(builder)
    if ((templates.isEmpty)) null else templates.get(0)
  }

  def getCategoryTemplates(project: Project, category: String): List[ReportTemplate] = {
    val builder = OqlBuilder.from(classOf[ReportTemplate], "rt")
    builder.where("rt.project =:project and rt.category=:category", project, category)
      .cacheable()
    entityDao.search(builder)
  }
}
