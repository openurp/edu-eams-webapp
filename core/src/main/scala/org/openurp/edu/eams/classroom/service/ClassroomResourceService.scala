package org.openurp.edu.eams.classroom.service

import org.openurp.base.Room
import org.beangle.commons.lang.time.YearWeekTime

trait RoomResourceService {

  def getFreeRooms(rooms: Iterable[Room], unit: YearWeekTime): Seq[Room]
}
