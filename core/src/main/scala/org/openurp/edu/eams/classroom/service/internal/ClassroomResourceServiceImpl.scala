package org.openurp.edu.eams.classroom.service.internal

import java.util.Collection
import java.util.Date
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.base.Classroom
import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.eams.classroom.service.ClassroomResourceService

import scala.collection.JavaConversions._

class ClassroomResourceServiceImpl extends BaseServiceImpl with ClassroomResourceService {

  def getFreeRooms(rooms: Collection[Classroom], unit: TimeUnit): List[Classroom] = {
    val query = OqlBuilder.from(classOf[Classroom], "classroom")
    query.where("not exists(select 1 from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom " + 
      "and (occupancy.time.year = :year) " + 
      "and (occupancy.time.weekState = :weekState) " + 
      "and (occupancy.time.weekday = :weekday) " + 
      "and (:startTime <= occupancy.time.endTime and :endTime > occupancy.time.startTime)" + 
      ")")
    if (!CollectUtils.isEmpty(rooms)) {
      query.where("classroom in (:rooms)")
    }
    query.where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)")
    query.orderBy("classroom.code")
    val params = CollectUtils.newHashMap()
    params.put("year", unit.year)
    params.put("weekState", unit.weekState)
    params.put("weekday", unit.getWeekday)
    params.put("startTime", unit.getStartTime)
    params.put("endTime", unit.getEndTime)
    if (!CollectUtils.isEmpty(rooms)) {
      params.put("rooms", rooms)
    }
    params.put("now", new Date())
    query.params(params)
    entityDao.search(query)
  }
}
