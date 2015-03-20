package org.openurp.edu.eams.teach.service

import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.base.Student



trait StudentTimeService {

  def isOccupied(time: YearWeekTime, stds: Student*): Boolean
}
