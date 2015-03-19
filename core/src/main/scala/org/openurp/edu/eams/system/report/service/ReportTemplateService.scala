package org.openurp.edu.eams.system.report.service


import org.openurp.edu.base.Project
import org.openurp.edu.eams.system.report.ReportTemplate


trait ReportTemplateService {

  def getTemplate(project: Project, code: String): ReportTemplate

  def getCategoryTemplates(project: Project, category: String): Seq[ReportTemplate]
}
