package org.openurp.edu.eams.teach.service

import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.base.Teacher



trait TeacherTimeService {

  def isOccupied(time: YearWeekTime, teachers: Teacher*): Boolean
}
