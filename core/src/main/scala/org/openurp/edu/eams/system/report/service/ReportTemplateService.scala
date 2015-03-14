package org.openurp.edu.eams.system.report.service

import java.util.List
import org.openurp.edu.base.Project
import org.openurp.edu.eams.system.report.ReportTemplate

import scala.collection.JavaConversions._

trait ReportTemplateService {

  def getTemplate(project: Project, code: String): ReportTemplate

  def getCategoryTemplates(project: Project, category: String): List[ReportTemplate]
}
