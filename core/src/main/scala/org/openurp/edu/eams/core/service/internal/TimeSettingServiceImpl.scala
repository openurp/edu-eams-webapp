package org.openurp.edu.eams.core.service.internal

import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.base.Building
import org.openurp.edu.eams.base.Campus
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.TimeSetting
import org.openurp.edu.base.Project
import org.openurp.edu.eams.core.service.TimeSettingService

import scala.collection.JavaConversions._

class TimeSettingServiceImpl extends BaseServiceImpl with TimeSettingService {

  def saveTimeSetting(setting: TimeSetting) {
    entityDao.saveOrUpdate(setting)
  }

  def removeTimeSetting(setting: TimeSetting) {
    entityDao.remove(setting)
  }

  def getTimeSetting(project: Project, semester: Semester, building: Building): TimeSetting = null

  def getClosestTimeSetting(project: Project, semester: Semester, campus: Campus): TimeSetting = {
    var builder = OqlBuilder.from(classOf[TimeSetting], "time").where("time.semester = :semester", semester)
      .where("exists(from " + classOf[Project].getName + 
      " p join p.timeSettings ts where ts.id=time and p = :project)", project)
      .where("time.campus = :campus", campus)
      .cacheable()
    var settings = entityDao.search(builder)
    if (!settings.isEmpty) return settings.get(0)
    builder = OqlBuilder.from(classOf[TimeSetting], "time").where("exists(from " + classOf[Project].getName + 
      " p join p.timeSettings ts where ts.id=time and p = :project)", project)
      .where("time.campus = :campus", campus)
      .where("time.semester is null")
      .cacheable()
    settings = entityDao.search(builder)
    if (!settings.isEmpty) return settings.get(0)
    builder = OqlBuilder.from(classOf[TimeSetting], "time").where("exists(from " + classOf[Project].getName + 
      " p join p.timeSettings ts where ts.id=time and p = :project)", project)
      .where("time.campus is null", campus)
      .where("time.semester =:semester", semester)
      .cacheable()
    settings = entityDao.search(builder)
    if (!settings.isEmpty) return settings.get(0)
    builder = OqlBuilder.from(classOf[TimeSetting], "time").where("exists(from " + classOf[Project].getName + 
      " p join p.timeSettings ts where ts.id=time and p = :project)", project)
      .where("time.campus is null and time.semester is null")
      .cacheable()
    settings = entityDao.search(builder)
    if (!settings.isEmpty) return settings.get(0)
    null
  }
}
