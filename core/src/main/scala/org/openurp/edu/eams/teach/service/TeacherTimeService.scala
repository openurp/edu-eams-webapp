package org.openurp.edu.eams.teach.service

import 
import org.openurp.edu.base.Teacher



trait TeacherTimeService {

  def isOccupied(time: YearWeekTime, teachers: Teacher*): Boolean
}
