package org.openurp.edu.eams.teach.schedule.service.impl

import java.util.Date



import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Building
import org.openurp.base.Campus
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.edu.eams.base.code.school.RoomType
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.eams.teach.schedule.service.ScheduleRoomService



class ScheduleRoomServiceImpl extends BaseServiceImpl with ScheduleRoomService {

  def getFreeRoomsOf(departments: Seq[Department], courseTimes: Array[CourseTime], activity: CourseActivity): OqlBuilder[Room] = {
    val hql = new StringBuilder(" from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom")
    val params = Collections.newMap[String,Any]
    var ocuupy = ""
    val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(activity.lesson, courseTimes)
    for (i <- 0 until timeUnits.length) {
      ocuupy = "(bitand(occupancy.time.state," + new java.lang.Long(timeUnits(i).state) + 
        ")>0 and occupancy.time.day = :weekday" + 
        i + 
        " and occupancy.time.year = :year" + 
        i + 
        " and occupancy.time.start < :endTime" + 
        i + 
        " and occupancy.time.end > :startTime" + 
        i + 
        ")"
      if (i > 0) {
        hql.append(" or ")
      } else if (i == 0) {
        hql.append(" and (")
      }
      hql.append(ocuupy)
      params.put("weekday" + i, new java.lang.Integer(timeUnits(i).day))
      params.put("endTime" + i, new java.lang.Integer(timeUnits(i).end))
      params.put("startTime" + i, new java.lang.Integer(timeUnits(i).start))
      params.put("year" + i, new java.lang.Integer(timeUnits(i).year))
    }
    hql.append(")")
    val builder = OqlBuilder.from(classOf[Room], "classroom").where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)")
    params.put("now", new Date())
    if (!departments.isEmpty) {
      builder.join("classroom.departments", "department")
        .where("department in (:departments)")
      params.put("departments", departments)
    }
    builder.select("select distinct classroom")
    val iterator = activity.rooms.iterator
    if (iterator.hasNext) {
      val classroom = iterator.next()
      builder.where("classroom.capacity >=:capacity", classroom.capacity)
      params.put("capacity", classroom.capacity)
      if (null != classroom.campus) {
        if (classroom.campus.persisted) {
          builder.where("classroom.campus = :campus")
          params.put("campus", classroom.campus)
        } else if (Strings.isNotBlank(classroom.campus.name)) {
          builder.where("classroom.campus.name like :campusName")
          params.put("campusName", "%" + classroom.campus.name + "%")
        }
      }
      if (null != classroom.building) {
        if (classroom.building.persisted) {
          builder.where("classroom.building = :building")
          params.put("building", classroom.building)
        } else if (Strings.isNotBlank(classroom.building.name)) {
          builder.where("classroom.building.name like :buildingName")
          params.put("buildingName", "%" + classroom.building.name + "%")
        }
      }
      if (Strings.isNotEmpty(classroom.name)) {
        builder.where(Condition.like("classroom.name", classroom.name))
      }
      if (Strings.isNotEmpty(classroom.code)) {
        builder.where(Condition.like("classroom.code", classroom.code))
      }
      if (null != classroom.roomType) {
        builder.where("classroom.type =:type")
        params.put("type", classroom.roomType)
      }
      if (classroom.floor != 0) {
        builder.where("(classroom.floor = :floor)")
        params.put("floor", classroom.floor)
      }
    }
    if (courseTimes.length > 0) {
      builder.where("not exists (" + hql.toString + ")")
    }
    builder.params(params)
    builder
  }

  def getOccupancyRoomsOf(departments: Seq[Department], courseTimes: Array[CourseTime], activity: CourseActivity): OqlBuilder[Room] = {
    val hql = new StringBuilder(" from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom")
    val params = Collections.newMap[String,Any]
    var ocuupy = ""
    val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(activity.lesson, courseTimes)
    for (i <- 0 until timeUnits.length) {
      ocuupy = "(bitand(occupancy.time.state," + new java.lang.Long(timeUnits(i).state) + 
        ")>0 and occupancy.time.day = :weekday" + 
        i + 
        " and occupancy.time.year = :year" + 
        i + 
        " and occupancy.time.start < :endTime" + 
        i + 
        " and occupancy.time.end > :startTime" + 
        i + 
        ")"
      if (i > 0) {
        hql.append(" or ")
      } else if (i == 0) {
        hql.append(" and (")
      }
      hql.append(ocuupy)
      params.put("weekday" + i, new java.lang.Integer(timeUnits(i).day))
      params.put("endTime" + i, new java.lang.Integer(timeUnits(i).end))
      params.put("startTime" + i, new java.lang.Integer(timeUnits(i).start))
      params.put("year" + i, new java.lang.Integer(timeUnits(i).year))
    }
    hql.append(")")
    val builder = OqlBuilder.from(classOf[Room], "classroom").where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)")
    params.put("now", new Date())
    if (!departments.isEmpty) {
      builder.join("classroom.departments", "department")
        .where("department in (:departments)")
      params.put("departments", departments)
    }
    builder.select("select distinct classroom")
    val iterator = activity.rooms.iterator
    if (iterator.hasNext) {
      val classroom = iterator.next()
      builder.where("classroom.capacity >=:capacity", classroom.capacity)
      params.put("capacity", classroom.capacity)
      if (null != classroom.campus) {
        if (classroom.campus.persisted) {
          builder.where("classroom.campus = :campus")
          params.put("campus", classroom.campus)
        } else if (Strings.isNotBlank(classroom.campus.name)) {
          builder.where("classroom.campus.name like :campusName")
          params.put("campusName", "%" + classroom.campus.name + "%")
        }
      }
      if (null != classroom.building) {
        if (classroom.building.persisted) {
          builder.where("classroom.building = :building")
          params.put("building", classroom.building)
        } else if (Strings.isNotBlank(classroom.building.name)) {
          builder.where("classroom.building.name like :buildingName")
          params.put("buildingName", "%" + classroom.building.name + "%")
        }
      }
      if (Strings.isNotEmpty(classroom.name)) {
        builder.where(Condition.like("classroom.name", classroom.name))
      }
      if (Strings.isNotEmpty(classroom.code)) {
        builder.where(Condition.like("classroom.code", classroom.code))
      }
      if (null != classroom.roomType) {
        builder.where("classroom.type =:type")
        params.put("type", classroom.roomType)
      }
      if (classroom.floor != 0) {
        builder.where("(classroom.floor = :floor)")
        params.put("floor", classroom.floor)
      }
    }
    if (courseTimes.length > 0) {
      builder.where("exists (" + hql.toString + ")")
    }
    builder.params(params)
    builder
  }

  def getFreeRoomsOfConditions(units: Array[YearWeekTime]): OqlBuilder[Room] = {
    val hql = new StringBuilder(" from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom")
    var ocuupy = ""
    for (i <- 0 until units.length) {
      ocuupy = "(bitand(occupancy.time.state," + new java.lang.Long(units(i).state) + 
        ")>0 and occupancy.time.day = " + 
        new java.lang.Integer(units(i).day) + 
        " and occupancy.time.year = " + 
        new java.lang.Integer(units(i).year) + 
        " and occupancy.time.start < " + 
        new java.lang.Integer(units(i).end) + 
        " and occupancy.time.end > " + 
        new java.lang.Integer(units(i).start) + 
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

  def getRooms(classroom: Room, departments: List[Department], limit: PageLimit): Seq[Room] = {
    val builder = OqlBuilder.from(classOf[Room], "classroom").where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)", 
      new java.util.Date())
      .limit(limit)
    if (!departments.isEmpty) {
      builder.where("classroom.building.department in (:departments)", departments)
    }
    if (null != classroom) {
      val campus = classroom.campus
      if (null != campus && campus.persisted) {
        builder.where("classroom.campus = :campus", campus)
      }
      if (null != classroom.id) {
        builder.where("(classroom.id = :id)", classroom.id)
      }
      if (Strings.isNotEmpty(classroom.code)) {
        builder.where(Condition.like("classroom.code", classroom.code))
      }
      if (Strings.isNotEmpty(classroom.name)) {
        builder.where(Condition.like("classroom.name", classroom.name))
      }
      val `type` = classroom.roomType
      if (null != `type` && `type`.persisted) {
        builder.where("classroom.type = :type", `type`)
      }
      val building = classroom.building
      if (null != building && building.persisted) {
        builder.where("classroom.building=:building", building)
      }
    }
    entityDao.search(builder)
  }
}
