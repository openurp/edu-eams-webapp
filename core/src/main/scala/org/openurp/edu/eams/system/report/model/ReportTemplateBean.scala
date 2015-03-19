package org.openurp.edu.eams.system.report.model

import org.openurp.edu.base.Project
import org.openurp.edu.eams.system.report.ReportTemplate
import org.beangle.data.model.bean.NumIdBean

class ReportTemplateBean extends NumIdBean[java.lang.Long] with ReportTemplate {

  var project: Project = _
  
  var category: String = _
  
  var code: String = _

  var name: String = _

  var remark: String = _

  var template: String = _

  var options: String = _
  
  var pageSize: String = "A4"
  
  var orientation: String = "Portrait"
}
