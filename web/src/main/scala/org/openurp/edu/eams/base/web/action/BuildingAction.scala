package org.openurp.edu.eams.base.web.action

import java.util.Collection
import java.util.Date
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.springframework.dao.DataIntegrityViolationException
import org.openurp.edu.eams.base.Building
import org.openurp.edu.eams.base.Campus
import org.openurp.base.Department
import org.openurp.edu.eams.base.model.BuildingBean

import scala.collection.JavaConversions._

class BuildingAction extends BaseInfoAction {

  def index(): String = {
    setting()
    forward()
  }

  def search(): String = {
    val query = buildOqlBuilder()
    query.limit(getPageLimit)
    put("buildings", entityDao.search(query))
    forward()
  }

  protected def buildOqlBuilder(): OqlBuilder[Building] = {
    val query = OqlBuilder.from(classOf[Building], "building")
    populateConditions(query)
    var orderBy = Params.get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "building.name"
    }
    query.orderBy(Order.parse(orderBy))
    query
  }

  def info(): String = {
    val buildingId = getIntId("building")
    if (buildingId == null || buildingId == 0) {
      return forwardError(Array("entity.building", "error.model.id.needed"))
    }
    val building = baseInfoService.getBaseInfo(classOf[Building], buildingId).asInstanceOf[Building]
    put("building", building)
    forward()
  }

  def edit(): String = {
    val building = getEntity(classOf[Building], "building")
    put("building", building)
    setting()
    forward()
  }

  protected def setting() {
    put("campuses", baseInfoService.getBaseInfos(classOf[Campus]))
    put("departments", baseInfoService.getBaseInfos(classOf[Department]))
  }

  def save(): String = {
    val building = populateEntity(classOf[BuildingBean], "building")
    val query = OqlBuilder.from(classOf[Building], "building")
    query.where("building.code = :code", building.getCode)
    if (null != building.getId) {
      query.where("building != :building", building)
    }
    if (CollectUtils.isNotEmpty(entityDao.search(query))) {
      setting()
      put("building", building)
      addError("error.code.existed")
      return "edit"
    }
    if (null == building.getId) {
      building.setCreatedAt(new Date())
      building.setUpdatedAt(building.getCreatedAt)
    } else {
      building.setUpdatedAt(new Date())
    }
    if (null == building.getSchool) building.setSchool(getSchool)
    entityDao.saveOrUpdate(building)
    logHelper.info((if (null == building.getId) "Create" else "Update") + 
      " a building with name: " + 
      building.getName)
    redirect("search", "info.save.success")
  }

  def remove(): String = {
    try {
      entityDao.remove(entityDao.get(classOf[Building], getIntIds("building")))
    } catch {
      case e: DataIntegrityViolationException => {
        logger.error(e.getMessage)
        addError(getText("error.remove.beenUsed"))
        search()
        return "search"
      }
    }
    redirect("search", "info.action.success")
  }

  def checkDuplicated(): String = {
    put("duplicated", CollectUtils.isNotEmpty(entityDao.get(classOf[Building], "code", get("code"))))
    forward()
  }

  protected def getExportDatas(): Collection[Building] = entityDao.search(buildOqlBuilder())

  def getEntityName(): String = classOf[Building].getName
}
