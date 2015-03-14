package org.openurp.edu.eams.base.web.action

import java.util.Collection
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.openurp.edu.eams.base.Building
import org.openurp.edu.eams.base.Campus
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.edu.eams.base.code.school.ClassroomType
import org.openurp.edu.eams.classroom.code.industry.RoomUsage
import org.openurp.edu.eams.core.service.ClassroomService

import scala.collection.JavaConversions._

class ClassroomSearchAction extends BaseInfoAction {

  def getEntityName(): String = classOf[Classroom].getName

  protected var classroomService: ClassroomService = _

  def index(): String = {
    prepare()
    forward()
  }

  protected def prepare() {
    put("classroomTypes", baseCodeService.getCodes(classOf[ClassroomType]))
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

  protected def buildOqlBuilder(): OqlBuilder[Classroom] = {
    val query = OqlBuilder.from(classOf[Classroom], "classroom")
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
    put("classroom", entityDao.get(classOf[Classroom], getIntId("classroom")))
    forward()
  }

  protected def getExportDatas(): Collection[Classroom] = {
    val classroomIds = getIntIds(getShortName)
    if (classroomIds.length == 0) {
      entityDao.search(buildOqlBuilder())
    } else {
      entityDao.get(classOf[Classroom], classroomIds)
    }
  }

  def setClassroomService(classroomService: ClassroomService) {
    this.classroomService = classroomService
  }
}
