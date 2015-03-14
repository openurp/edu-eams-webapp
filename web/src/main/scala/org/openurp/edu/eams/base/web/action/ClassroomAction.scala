package org.openurp.edu.eams.base.web.action

import java.util.Date
import java.util.List
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.transfer.TransferListener
import org.beangle.commons.transfer.importer.listener.ImporterForeignerListener
import org.springframework.dao.DataIntegrityViolationException
import org.openurp.edu.eams.base.Building
import org.openurp.edu.eams.base.Campus
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.edu.eams.base.code.school.ClassroomType
import org.openurp.edu.eams.base.model.ClassroomBean
import org.openurp.edu.eams.classroom.RoomUsageCapacity
import org.openurp.edu.eams.classroom.code.industry.RoomUsage
import org.openurp.edu.eams.core.service.listener.ClassroomImportListener

import scala.collection.JavaConversions._

class ClassroomAction extends ClassroomSearchAction {

  def edit(): String = {
    put("classroomTypes", baseCodeService.getCodes(classOf[ClassroomType]))
    put("buildings", baseInfoService.getBaseInfos(classOf[Building]))
    put("campuses", baseInfoService.getBaseInfos(classOf[Campus]))
    put("usages", baseCodeService.getCodes(classOf[RoomUsage]))
    val classroom = getEntity(classOf[Classroom], "classroom")
    put("classroom", classroom)
    val departments = baseInfoService.getBaseInfos(classOf[Department])
    val classroomDepartments = classroom.departments
    val showDepartments = CollectUtils.newArrayList()
    for (department <- departments if !classroomDepartments.contains(department)) {
      showDepartments.add(department)
    }
    put("departments", showDepartments)
    forward()
  }

  def save(): String = {
    val room = populateEntity(classOf[ClassroomBean], "classroom")
    val query = OqlBuilder.from(classOf[Classroom], "room")
    query.where("room.code = :code", room.getCode)
    if (null != room.getId) {
      query.where("room != :room", room)
    }
    if (CollectUtils.isNotEmpty(entityDao.search(query))) {
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
    if (null == room.getId) room.setCreatedAt(new Date())
    room.setUpdatedAt(new Date())
    if (null == room.getSchool) room.setSchool(getSchool)
    room.departments.clear()
    room.departments.addAll(entityDao.get(classOf[Department], getAll("selectDepartment.id", classOf[Integer])))
    entityDao.saveOrUpdate(room)
    logHelper.info((if (null == room.getId) "Create" else "Update") + " a classroom with name: " + 
      room.getName)
    redirect("search", "info.save.success")
  }

  def remove(): String = {
    try {
      entityDao.remove(entityDao.get(classOf[Classroom], getIntIds("classroom")))
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
    put("duplicated", CollectUtils.isNotEmpty(entityDao.get(classOf[Classroom], "code", get("code"))))
    forward()
  }

  protected def getImporterListeners(): List[TransferListener] = {
    val listeners = CollectUtils.newArrayList()
    listeners.add(new ImporterForeignerListener(entityDao))
    listeners.add(new ClassroomImportListener(entityDao, "code", getProject.getSchool))
    listeners
  }
}
