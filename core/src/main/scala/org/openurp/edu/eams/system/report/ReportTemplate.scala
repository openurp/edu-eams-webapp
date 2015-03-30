package org.openurp.edu.eams.system.report

import org.openurp.edu.base.Project
import org.beangle.data.model.LongIdEntity

trait ReportTemplate extends LongIdEntity {
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
