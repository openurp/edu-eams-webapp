package org.openurp.edu.eams.teach.schedule.service.propertyExtractor

import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.transfer.exporter.DefaultPropertyExtractor
import org.openurp.edu.eams.teach.schedule.model.CurriculumChangeApplication

import scala.collection.JavaConversions._

class CurriculumChangePropertyExtractor(textResource: TextResource) extends DefaultPropertyExtractor(textResource) {

  def getPropertyValue(target: AnyRef, property: String): AnyRef = {
    val curriculumChangeApplication = target.asInstanceOf[CurriculumChangeApplication]
    if ("passed" == property) {
      if (curriculumChangeApplication.getPassed == null) {
        "未审核"
      } else if (true == curriculumChangeApplication.getPassed) {
        "审核通过"
      } else {
        "审核未通过"
      }
    } else if ("remark" == property) {
      Strings.replace(curriculumChangeApplication.getRemark, "<br/>", ";")
    } else {
      super.getPropertyValue(target, property)
    }
  }
}
