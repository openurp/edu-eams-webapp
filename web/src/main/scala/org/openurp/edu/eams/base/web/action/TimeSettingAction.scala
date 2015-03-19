package org.openurp.edu.eams.base.web.action

import org.beangle.commons.entity.util.ValidEntityKeyPredicate
import org.beangle.commons.lang.Strings
import org.openurp.base.TimeSetting
import org.openurp.edu.eams.base.model.DefaultCourseUnitBean
import org.openurp.edu.eams.base.model.TimeSettingBean
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction
import TimeSettingAction._




object TimeSettingAction {

  private val maxUnit = 20

  def getTimeNumber(time: String): Int = getTimeNumber(time, ":")

  def getTimeNumber(time: String, delimter: String): Int = {
    val index = time.indexOf(delimter)
    java.lang.Integer.parseInt(time.substring(0, index) + time.substring(index + 1, index + 3))
  }
}

class TimeSettingAction extends RestrictionSupportAction {

  
  var timeSettingService: TimeSettingService = _

  def index(): String = forward()

  def edit(): String = {
    val settingId = getIntId("timeSetting")
    var setting: TimeSetting = null
    if (null == settingId) {
      setting = new TimeSettingBean()
    } else {
      setting = entityDao.get(classOf[TimeSetting], settingId).asInstanceOf[TimeSetting]
      if (null == setting) return forwardError("error.model.notExist")
      if (!inAuthority(setting)) return forwardError("error.dataRealm.insufficient")
    }
    put("timeSetting", setting)
    put("project", getProject)
    forward()
  }

  def info(): String = {
    val settingId = getIntId("timeSetting")
    if (null == settingId) forwardError("error.model.id.needed") else {
      val setting = entityDao.get(classOf[TimeSetting], settingId).asInstanceOf[TimeSetting]
      if (null == setting) return forwardError("error.model.notExist")
      put("timeSetting", setting)
      forward()
    }
  }

  def save(): String = {
    val settingId = getInt("timeSetting.id")
    var setting: TimeSettingBean = null
    var isNew = false
    if (!ValidEntityKeyPredicate.Instance.apply(settingId)) {
      setting = new TimeSettingBean()
      setting.setSchool(getSchool)
      isNew = true
    } else {
      setting = entityDao.get(classOf[TimeSettingBean], settingId)
      if (null == setting) return forwardError("error.model.notExist")
      if (!inAuthority(setting)) return forwardError("error.dataRealm.insufficient")
    }
    for (i <- 0 until maxUnit) {
      val unitId = getInt(i + "id")
      var unit: DefaultCourseUnitBean = null
      val name = get(i + "name")
      unit = if (null != unitId) entityDao.get(classOf[DefaultCourseUnitBean], unitId) else new DefaultCourseUnitBean()
      if (Strings.isBlank(name)) {
        if (null != unitId) {
          setting.getDefaultUnits.remove(unit.getIndexno)
          entityDao.remove(unit)
        }
        //continue
      }
      unit.setName(name)
      unit.setIndexno(i + 1)
      unit.setEngName(get(i + "engName"))
      unit.setStartTime(getTimeNumber(get(i + "startTime")))
      unit.setEndTime(getTimeNumber(get(i + "endTime")))
      unit.setColor(get(i + "color"))
      if (unit.id == null) {
        setting.getDefaultUnits.put(unit.getIndexno, unit)
        unit.setTimeSetting(setting)
      }
    }
    setting.setName(get("timeSetting.name"))
    timeSettingService.saveTimeSetting(setting)
    if (isNew) {
      redirect("list", "info.save.success", "settingId=" + setting.id)
    } else {
      redirect("list", "info.save.success", "settingId=" + setting.id)
    }
  }

  def list(): String = {
    put("settings", entityDao.getAll(classOf[TimeSetting]))
    forward()
  }

  def remove(): String = {
    val settingId = getIntId("timeSetting")
    var setting: TimeSetting = null
    if (null == settingId) {
      return forwardError("error.model.id.needed")
    } else {
      setting = entityDao.get(classOf[TimeSettingBean], settingId)
      if (null == setting) return forwardError("error.model.notExist")
      if (!inAuthority(setting)) return forwardError("error.dataRealm.insufficient")
    }
    var deleteMsg: String = null
    if (settingId == new java.lang.Integer(1)) deleteMsg = "error.timeSetting.defaultCannotbeDeleted" else {
      deleteMsg = "info.delete.success"
      try {
        timeSettingService.removeTimeSetting(setting)
      } catch {
        case e: Exception => deleteMsg = "info.delete.failure"
      }
    }
    redirect("index", deleteMsg)
  }

  private def inAuthority(setting: TimeSetting): Boolean = isAdmin

  def getMaxUnit(): Int = maxUnit
}
