package org.openurp.edu.eams.classroom.service



import org.openurp.base.Room
import 



trait RoomResourceService {

  def getFreeRooms(rooms: Iterable[Room], unit: YearWeekTime): List[Room]
}
