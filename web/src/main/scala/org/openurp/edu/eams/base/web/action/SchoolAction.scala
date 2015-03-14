package org.openurp.edu.eams.base.web.action

import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.openurp.edu.eams.base.School
import org.openurp.edu.eams.base.code.ministry.Institution
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

class SchoolAction extends BaseAction {

  protected def getEntityName(): String = classOf[School].getName

  protected def indexSetting() {
  }

  protected def editSetting(entity: Entity[_]) {
    val builder = OqlBuilder.from(classOf[Institution], "institution")
      .where("institution.effectiveAt <= :now and (institution.invalidAt is null or institution.invalidAt >= :now)", 
      new java.util.Date())
    if (entity.isPersisted) {
      builder.where("not exists(from " + classOf[School].getName + 
        " school where school.institution = institution and institution <> :institution)", entity.asInstanceOf[School].getInstitution)
    } else {
      builder.where("not exists(from " + classOf[School].getName + " school where school.institution = institution)")
    }
    builder.orderBy("institution.code")
    put("institutions", entityDao.search(builder))
  }
}
