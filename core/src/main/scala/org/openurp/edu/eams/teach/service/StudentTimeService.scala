package org.openurp.edu.eams.teach.service

import 
import org.openurp.edu.base.Student



trait StudentTimeService {

  def isOccupied(time: YearWeekTime, stds: Student*): Boolean
}
