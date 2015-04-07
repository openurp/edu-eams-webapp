package org.openurp.edu.eams.teach.schedule.service


import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Room
import org.openurp.base.Department
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.teach.schedule.CourseActivity



trait ScheduleRoomService {

  def getFreeRoomsOf(departments: List[Department], courseTimes: Array[CourseTime], activity: CourseActivity): OqlBuilder[Room]

  def getOccupancyRoomsOf(departments: List[Department], courseTimes: Array[CourseTime], activity: CourseActivity): OqlBuilder[Room]

  def getFreeRoomsOfConditions(units: Array[YearWeekTime]): OqlBuilder[Room]

  def getRooms(classroom: Room, departments: List[Department], pageLimit: PageLimit): Seq[Room]
}
