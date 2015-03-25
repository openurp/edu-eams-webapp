package org.openurp.edu.eams.base.web.action


import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.openurp.edu.eams.base.Building
import org.openurp.base.Campus
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.edu.eams.base.code.school.RoomType
import org.openurp.base.code.RoomUsage
import org.openurp.edu.eams.core.service.RoomService



class RoomSearchAction extends BaseInfoAction {

  def getEntityName(): String = classOf[Room].getName

  var classroomService: RoomService = _

  def index(): String = {
    prepare()
    forward()
  }

  protected def prepare() {
    put("classroomTypes", baseCodeService.getCodes(classOf[RoomType]))
    put("buildings", baseInfoService.getBaseInfos(classOf[Building]))
    put("campuses", baseInfoService.getBaseInfos(classOf[Campus]))
    put("departments", baseInfoService.getBaseInfos(classOf[Department]))
    put("usages", baseCodeService.getCodes(classOf[RoomUsage]))
  }

  def search(): String = {
    val query = buildOqlBuilder()
    query.limit(getPageLimit)
    put("classrooms", entityDao.search(query))
    forward()
  }

  protected def buildOqlBuilder(): OqlBuilder[Room] = {
    val query = OqlBuilder.from(classOf[Room], "classroom")
    populateConditions(query)
    query.join("left outer", "classroom.building", "building")
    var orderBy = Params.get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "classroom.name"
    }
    val usageCapacityId = getInt("usageCapacityId")
    if (null != usageCapacityId) {
      query.where("exists (from classroom.usages usage where usage.usage.id = :usageCapacityId)", usageCapacityId)
    }
    query.orderBy(Order.parse(orderBy))
    query
  }

  def info(): String = {
    put("classroom", entityDao.get(classOf[Room], getIntId("classroom")))
    forward()
  }

  protected def getExportDatas(): Iterable[Room] = {
    val classroomIds = getIntIds(getShortName)
    if (classroomIds.length == 0) {
      entityDao.search(buildOqlBuilder())
    } else {
      entityDao.get(classOf[Room], classroomIds)
    }
  }
}
