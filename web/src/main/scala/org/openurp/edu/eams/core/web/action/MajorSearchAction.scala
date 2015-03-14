package org.openurp.edu.eams.core.web.action

import java.util.Collection
import java.util.Date
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Major
import org.openurp.edu.eams.core.code.ministry.DisciplineCatalog
import org.openurp.edu.eams.web.action.common.ProjectSupportAction

import scala.collection.JavaConversions._

class MajorSearchAction extends ProjectSupportAction {

  def getEntityName(): String = classOf[Major].getName

  def index(): String = {
    put("project", getProject)
    put("departments", getDeparts)
    put("educations", getEducations)
    put("catalogs", baseCodeService.getCodes(classOf[DisciplineCatalog]))
    forward()
  }

  def search(): String = {
    put("majors", entityDao.search(buildMajorQuery()))
    forward()
  }

  protected def buildMajorQuery(): OqlBuilder[Major] = {
    val query = OqlBuilder.from(classOf[Major], "major")
    populateConditions(query)
    query.where("major.project = :project", getProject)
    val departId = getInt("fake.department.id")
    if (departId != null) {
      query.where("exists(from major.journals md where md.depart.id = :departId)", departId)
    }
    val educationId = getInt("fake.education.id")
    if (educationId != null) {
      query.where("exists(from major.journals md where md.education.id = :educationId)", educationId)
    }
    var orderBy = get("orderBy")
    if (Strings.isEmpty(orderBy)) orderBy = "major.code"
    val active = getBoolean("active")
    if (null != active) {
      if (active) query.where("major.effectiveAt <= :now and (major.invalidAt = null or major.invalidAt > :now)", 
        new Date()) else query.where("major.effectiveAt > :now or major.invalidAt <= :now", new Date())
    }
    query.limit(getPageLimit)
    query.orderBy(Order.parse(orderBy))
    query
  }

  protected def getExportDatas(): Collection[Major] = {
    val query = buildMajorQuery()
    val majorIds = getIntIds("major")
    if (majorIds != null && majorIds.length > 0) {
      query.where("major.id in (:majorIds)", majorIds)
    }
    query.limit(null)
    entityDao.search(query)
  }

  def info(): String = {
    val majorId = getIntId("major")
    if (null == majorId) {
      return forwardError(Array("entity.major", "error.model.id.needed"))
    }
    put("major", entityDao.get(classOf[Major], majorId))
    forward()
  }
}
