package org.openurp.edu.eams.core.service

import org.openurp.base.Building
import org.openurp.edu.eams.base.Campus
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.TimeSetting
import org.openurp.edu.base.Project

import scala.collection.JavaConversions._

trait TimeSettingService {

  def getTimeSetting(project: Project, semester: Semester, building: Building): TimeSetting

  def saveTimeSetting(setting: TimeSetting): Unit

  def removeTimeSetting(setting: TimeSetting): Unit

  def getClosestTimeSetting(project: Project, semester: Semester, campus: Campus): TimeSetting
}
