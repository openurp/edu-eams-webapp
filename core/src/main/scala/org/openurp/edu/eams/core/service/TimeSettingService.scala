package org.openurp.edu.eams.core.service

import org.openurp.base.Building
import org.openurp.base.Campus
import org.openurp.base.Semester
import org.openurp.base.TimeSetting
import org.openurp.edu.base.Project



trait TimeSettingService {

  def getTimeSetting(project: Project, semester: Semester, building: Building): TimeSetting

  def saveTimeSetting(setting: TimeSetting): Unit

  def removeTimeSetting(setting: TimeSetting): Unit

  def getClosestTimeSetting(project: Project, semester: Semester, campus: Campus): TimeSetting
}
