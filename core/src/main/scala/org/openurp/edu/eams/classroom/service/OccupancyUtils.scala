package org.openurp.edu.eams.classroom.service

import java.util.Date
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.base.Classroom
import org.openurp.edu.eams.classroom.RoomUsageCapacity
import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.eams.classroom.code.industry.RoomUsage

import scala.collection.JavaConversions._

object OccupancyUtils {

  def getFreeRoomsOfConditions(units: Array[TimeUnit]): OqlBuilder[Classroom] = {
    val hql = new StringBuilder(" from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom")
    var ocuupy = ""
    for (i <- 0 until units.length) {
      ocuupy = "(bitand(occupancy.time.weekStateNum," + new java.lang.Long(units(i).getWeekStateNum) + 
        ")>0 and occupancy.time.weekday = " + 
        new java.lang.Integer(units(i).getWeekday) + 
        " and occupancy.time.year = " + 
        new java.lang.Integer(units(i).getYear) + 
        " and occupancy.time.startTime < " + 
        new java.lang.Integer(units(i).getEndTime) + 
        " and occupancy.time.endTime > " + 
        new java.lang.Integer(units(i).getStartTime) + 
        ")"
      if (i > 0) {
        hql.append(" or ")
      } else if (i == 0) {
        hql.append(" and (")
      }
      hql.append(ocuupy)
    }
    hql.append(")")
    val query = OqlBuilder.from(classOf[Classroom], "classroom").where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)", 
      new Date())
      .where("not exists (" + hql.toString + ")")
    query
  }

  def buildFreeroomQuery(usage: RoomUsage, units: Array[TimeUnit]): OqlBuilder[RoomUsageCapacity] = {
    val hql = new StringBuilder(" from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom")
    var ocuupy = ""
    for (i <- 0 until units.length) {
      ocuupy = "(bitand(occupancy.time.weekStateNum," + new java.lang.Long(units(i).getWeekStateNum) + 
        ")>0 and occupancy.time.weekday = " + 
        new java.lang.Integer(units(i).getWeekday) + 
        " and occupancy.time.year = " + 
        new java.lang.Integer(units(i).getYear) + 
        " and occupancy.time.startTime < " + 
        new java.lang.Integer(units(i).getEndTime) + 
        " and occupancy.time.endTime > " + 
        new java.lang.Integer(units(i).getStartTime) + 
        ")"
      if (i > 0) {
        hql.append(" or ")
      } else if (i == 0) {
        hql.append(" and (")
      }
      hql.append(ocuupy)
    }
    hql.append(")")
    val query = OqlBuilder.from(classOf[RoomUsageCapacity], "roomUsageCapacity")
      .join("roomUsageCapacity.room", "classroom")
      .where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)", 
      new Date())
      .where("not exists (" + hql.toString + ")")
      .where("roomUsageCapacity.usage=:usage", usage)
    query
  }
}
