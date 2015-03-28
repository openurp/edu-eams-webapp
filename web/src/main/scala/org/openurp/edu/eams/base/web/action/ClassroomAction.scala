package org.openurp.edu.eams.base.web.action

import java.util.Date


import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.transfer.TransferListener
import org.beangle.commons.transfer.importer.listener.ImporterForeignerListener
import org.springframework.dao.DataIntegrityViolationException
import org.openurp.edu.eams.base.Building
import org.openurp.base.Campus
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.edu.eams.base.code.school.RoomType
import org.openurp.edu.eams.base.model.RoomBean
import org.openurp.edu.eams.classroom.RoomUsageCapacity
import org.openurp.base.code.RoomUsage
import org.openurp.edu.eams.core.service.listener.RoomImportListener



class RoomAction extends RoomSearchAction {

  def edit(): String = {
    put("classroomTypes", baseCodeService.getCodes(classOf[RoomType]))
    put("buildings", baseInfoService.getBaseInfos(classOf[Building]))
    put("campuses", baseInfoService.getBaseInfos(classOf[Campus]))
    put("usages", baseCodeService.getCodes(classOf[RoomUsage]))
    val classroom = getEntity(classOf[Room], "classroom")
    put("classroom", classroom)
    val departments = baseInfoService.getBaseInfos(classOf[Department])
    val classroomDepartments = classroom.departments
    val showDepartments = Collections.newBuffer[Any]
    for (department <- departments if !classroomDepartments.contains(department)) {
      showDepartments.add(department)
    }
    put("departments", showDepartments)
    forward()
  }

  def save(): String = {
    val room = populateEntity(classOf[RoomBean], "classroom")
    val query = OqlBuilder.from(classOf[Room], "room")
    query.where("room.code = :code", room.getCode)
    if (null != room.id) {
      query.where("room != :room", room)
    }
    if (Collections.isNotEmpty(entityDao.search(query))) {
      prepare()
      put("classroom", room)
      addError(getText("error.code.existed"))
      return "edit"
    }
    room.getUsages.clear()
    val ruCount = getInt("ruCount")
    for (i <- 0 until ruCount.intValue()) {
      val usageCapacityId = getLong("roomUsage" + i + ".usage.id")
      if (null != usageCapacityId) {
        val roomUsage = populateEntity(classOf[RoomUsageCapacity], "roomUsage" + i)
        roomUsage.setRoom(room)
        room.getUsages.add(roomUsage)
      }
    }
    if (null == room.id) room.setCreatedAt(new Date())
    room.setUpdatedAt(new Date())
    if (null == room.getSchool) room.setSchool(getSchool)
    room.departments.clear()
    room.departments.addAll(entityDao.get(classOf[Department], getAll("selectDepartment.id", classOf[Integer])))
    entityDao.saveOrUpdate(room)
    logHelper.info((if (null == room.id) "Create" else "Update") + " a classroom with name: " + 
      room.getName)
    redirect("search", "info.save.success")
  }

  def remove(): String = {
    try {
      entityDao.remove(entityDao.get(classOf[Room], getIntIds("classroom")))
      redirect("search", "info.action.success")
    } catch {
      case e: DataIntegrityViolationException => {
        logger.error(e.getMessage)
        addError(getText("error.remove.beenUsed"))
        search()
        "search"
      }
    }
  }

  def checkDuplicated(): String = {
    put("duplicated", Collections.isNotEmpty(entityDao.get(classOf[Room], "code", get("code"))))
    forward()
  }

  protected def getImporterListeners(): List[TransferListener] = {
    val listeners = Collections.newBuffer[Any]
    listeners.add(new ImporterForeignerListener(entityDao))
    listeners.add(new RoomImportListener(entityDao, "code", getProject.getSchool))
    listeners
  }
}
