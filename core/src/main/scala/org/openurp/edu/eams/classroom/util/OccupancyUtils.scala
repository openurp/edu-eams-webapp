package org.openurp.edu.eams.classroom.util

import java.util.Date
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Room
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.base.code.RoomUsage
import org.openurp.lg.room.UsageCapacity

object OccupancyUtils {

  def getFreeRoomsOfConditions(units: Array[YearWeekTime]): OqlBuilder[Room] = {
    val hql = new StringBuilder(" from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom")
    var ocuupy = ""
    for (i <- 0 until units.length) {
      ocuupy = "(bitand(occupancy.time.state," + units(i).state + 
        ")>0 and occupancy.time.day = " + 
        units(i).day.id + 
        " and occupancy.time.year = " + 
        new java.lang.Integer(units(i).year) + 
        " and occupancy.time.start < " + 
        units(i).end.value + 
        " and occupancy.time.end > " + 
        units(i).begin.value + 
        ")"
      if (i > 0) {
        hql.append(" or ")
      } else if (i == 0) {
        hql.append(" and (")
      }
      hql.append(ocuupy)
    }
    hql.append(")")
    val query = OqlBuilder.from(classOf[Room], "classroom").where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)", 
      new Date())
      .where("not exists (" + hql.toString + ")")
    query
  }

  def buildFreeroomQuery(usage: RoomUsage, units: Array[YearWeekTime]): OqlBuilder[UsageCapacity] = {
    val hql = new StringBuilder(" from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom")
    var ocuupy = ""
    for (i <- 0 until units.length) {
      ocuupy = "(bitand(occupancy.time.state," +units(i).state + 
        ")>0 and occupancy.time.day = " + 
        units(i).day.id + 
        " and occupancy.time.year = " + 
        units(i).year + 
        " and occupancy.time.start < " + 
       units(i).end.value + 
        " and occupancy.time.end > " + 
       units(i).begin.value + 
        ")"
      if (i > 0) {
        hql.append(" or ")
      } else if (i == 0) {
        hql.append(" and (")
      }
      hql.append(ocuupy)
    }
    hql.append(")")
    val query = OqlBuilder.from(classOf[UsageCapacity], "roomUsageCapacity")
      .join("roomUsageCapacity.room", "classroom")
      .where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)", 
      new Date())
      .where("not exists (" + hql.toString + ")")
      .where("roomUsageCapacity.usage=:usage", usage)
    query
  }
}
