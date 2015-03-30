package org.openurp.edu.eams.classroom.service.internal


import java.util.Date
import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Room
import org.openurp.edu.eams.classroom.service.RoomResourceService
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.eams.classroom.service.RoomResourceService

class RoomResourceServiceImpl extends BaseServiceImpl with RoomResourceService {

  def getFreeRooms(rooms: Iterable[Room], unit: YearWeekTime): Seq[Room] = {
    val query = OqlBuilder.from(classOf[Room], "classroom")
    query.where("not exists(select 1 from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom " + 
      "and (occupancy.time.year = :year) " + 
      "and (occupancy.time.state = :weekState) " + 
      "and (occupancy.time.day = :weekday) " + 
      "and (:startTime <= occupancy.time.end and :endTime > occupancy.time.start)" + 
      ")")
    if (!Collections.isEmpty(rooms)) {
      query.where("classroom in (:rooms)")
    }
    query.where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)")
    query.orderBy("classroom.code")
    val params = Collections.newMap[String,Any]
    params.put("year", unit.year)
    params.put("weekState", unit.state)
    params.put("weekday", unit.day)
    params.put("startTime", unit.begin)
    params.put("endTime", unit.end)
    if (!Collections.isEmpty(rooms)) {
      params.put("rooms", rooms)
    }
    params.put("now", new Date())
    query.params(params)
    entityDao.search(query)
  }
}
