package org.openurp.edu.eams.teach.program.exporter

import org.beangle.data.model.dao.EntityDao
import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.transfer.exporter.DefaultPropertyExtractor
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean
//remove if not needed


class CourseGroupModifyPropertyExtractor(resource: TextResource) extends DefaultPropertyExtractor(resource) {

  protected var textResource: TextResource = _

  protected var entityDao: EntityDao = _

  def setTextResource(textResource: TextResource) {
    this.textResource = textResource
  }

  def getPropertyValue(target: AnyRef, property: String): AnyRef = {
    val modify = target.asInstanceOf[MajorCourseGroupModifyBean]
    val plan = entityDao.get(classOf[MajorPlan], modify.getMajorPlan.id)
    if (property == "flag") {
      return toString(modify.getFlag)
    } else if (property == "majorPlan.program.grade") {
      if (plan == null) {
        return "培养计划已不存在"
      } else {
        return plan.getProgram.getGrade
      }
    } else if (property == "majorPlan.program.major.name") {
      if (plan == null) {
        return "培养计划已不存在"
      } else {
        return plan.getProgram.getMajor.getName
      }
    } else if (property == "majorPlan.program.direction.name") {
      if (plan == null) {
        return "培养计划已不存在"
      } else {
        if (plan.getProgram.getDirection != null) {
          return plan.getProgram.getDirection.getName
        }
      }
    }
    super.getPropertyValue(target, property)
  }

  private def toString(status: Int): String = {
    if (status == -1) {
      "申请"
    } else if (status == 1) {
      "接受"
    } else {
      "拒绝"
    }
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
