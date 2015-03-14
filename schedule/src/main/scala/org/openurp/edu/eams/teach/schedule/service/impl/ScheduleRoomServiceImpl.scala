package org.openurp.edu.eams.teach.schedule.service.impl

import java.util.Date
import java.util.Iterator
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Building
import org.openurp.edu.eams.base.Campus
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.edu.eams.base.code.school.ClassroomType
import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.eams.teach.lesson.util.TimeUnitUtil
import org.openurp.edu.eams.teach.schedule.service.ScheduleRoomService

import scala.collection.JavaConversions._

class ScheduleRoomServiceImpl extends BaseServiceImpl with ScheduleRoomService {

  def getFreeRoomsOf(departments: List[Department], courseTimes: Array[CourseTime], activity: CourseActivity): OqlBuilder[Classroom] = {
    val hql = new StringBuilder(" from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom")
    val params = CollectUtils.newHashMap()
    var ocuupy = ""
    val timeUnits = TimeUnitUtil.convertToTimeUnits(activity.getLesson, courseTimes)
    for (i <- 0 until timeUnits.length) {
      ocuupy = "(bitand(occupancy.time.weekStateNum," + new java.lang.Long(timeUnits(i).getWeekStateNum) + 
        ")>0 and occupancy.time.weekday = :weekday" + 
        i + 
        " and occupancy.time.year = :year" + 
        i + 
        " and occupancy.time.startTime < :endTime" + 
        i + 
        " and occupancy.time.endTime > :startTime" + 
        i + 
        ")"
      if (i > 0) {
        hql.append(" or ")
      } else if (i == 0) {
        hql.append(" and (")
      }
      hql.append(ocuupy)
      params.put("weekday" + i, new java.lang.Integer(timeUnits(i).getWeekday))
      params.put("endTime" + i, new java.lang.Integer(timeUnits(i).getEndTime))
      params.put("startTime" + i, new java.lang.Integer(timeUnits(i).getStartTime))
      params.put("year" + i, new java.lang.Integer(timeUnits(i).getYear))
    }
    hql.append(")")
    val builder = OqlBuilder.from(classOf[Classroom], "classroom").where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)")
    params.put("now", new Date())
    if (!departments.isEmpty) {
      builder.join("classroom.departments", "department")
        .where("department in (:departments)")
      params.put("departments", departments)
    }
    builder.select("select distinct classroom")
    val iterator = activity.getRooms.iterator()
    if (iterator.hasNext) {
      val classroom = iterator.next()
      builder.where("classroom.capacity >=:capacity", classroom.getCapacity)
      params.put("capacity", classroom.getCapacity)
      if (null != classroom.getCampus) {
        if (classroom.getCampus.isPersisted) {
          builder.where("classroom.campus = :campus")
          params.put("campus", classroom.getCampus)
        } else if (Strings.isNotBlank(classroom.getCampus.getName)) {
          builder.where("classroom.campus.name like :campusName")
          params.put("campusName", "%" + classroom.getCampus.getName + "%")
        }
      }
      if (null != classroom.getBuilding) {
        if (classroom.getBuilding.isPersisted) {
          builder.where("classroom.building = :building")
          params.put("building", classroom.getBuilding)
        } else if (Strings.isNotBlank(classroom.getBuilding.getName)) {
          builder.where("classroom.building.name like :buildingName")
          params.put("buildingName", "%" + classroom.getBuilding.getName + "%")
        }
      }
      if (Strings.isNotEmpty(classroom.getName)) {
        builder.where(Condition.like("classroom.name", classroom.getName))
      }
      if (Strings.isNotEmpty(classroom.getCode)) {
        builder.where(Condition.like("classroom.code", classroom.getCode))
      }
      if (null != classroom.getType) {
        builder.where("classroom.type =:type")
        params.put("type", classroom.getType)
      }
      if (classroom.getFloor != 0) {
        builder.where("(classroom.floor = :floor)")
        params.put("floor", classroom.getFloor)
      }
    }
    if (courseTimes.length > 0) {
      builder.where("not exists (" + hql.toString + ")")
    }
    builder.params(params)
    builder
  }

  def getOccupancyRoomsOf(departments: List[Department], courseTimes: Array[CourseTime], activity: CourseActivity): OqlBuilder[Classroom] = {
    val hql = new StringBuilder(" from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom")
    val params = CollectUtils.newHashMap()
    var ocuupy = ""
    val timeUnits = TimeUnitUtil.convertToTimeUnits(activity.getLesson, courseTimes)
    for (i <- 0 until timeUnits.length) {
      ocuupy = "(bitand(occupancy.time.weekStateNum," + new java.lang.Long(timeUnits(i).getWeekStateNum) + 
        ")>0 and occupancy.time.weekday = :weekday" + 
        i + 
        " and occupancy.time.year = :year" + 
        i + 
        " and occupancy.time.startTime < :endTime" + 
        i + 
        " and occupancy.time.endTime > :startTime" + 
        i + 
        ")"
      if (i > 0) {
        hql.append(" or ")
      } else if (i == 0) {
        hql.append(" and (")
      }
      hql.append(ocuupy)
      params.put("weekday" + i, new java.lang.Integer(timeUnits(i).getWeekday))
      params.put("endTime" + i, new java.lang.Integer(timeUnits(i).getEndTime))
      params.put("startTime" + i, new java.lang.Integer(timeUnits(i).getStartTime))
      params.put("year" + i, new java.lang.Integer(timeUnits(i).getYear))
    }
    hql.append(")")
    val builder = OqlBuilder.from(classOf[Classroom], "classroom").where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)")
    params.put("now", new Date())
    if (!departments.isEmpty) {
      builder.join("classroom.departments", "department")
        .where("department in (:departments)")
      params.put("departments", departments)
    }
    builder.select("select distinct classroom")
    val iterator = activity.getRooms.iterator()
    if (iterator.hasNext) {
      val classroom = iterator.next()
      builder.where("classroom.capacity >=:capacity", classroom.getCapacity)
      params.put("capacity", classroom.getCapacity)
      if (null != classroom.getCampus) {
        if (classroom.getCampus.isPersisted) {
          builder.where("classroom.campus = :campus")
          params.put("campus", classroom.getCampus)
        } else if (Strings.isNotBlank(classroom.getCampus.getName)) {
          builder.where("classroom.campus.name like :campusName")
          params.put("campusName", "%" + classroom.getCampus.getName + "%")
        }
      }
      if (null != classroom.getBuilding) {
        if (classroom.getBuilding.isPersisted) {
          builder.where("classroom.building = :building")
          params.put("building", classroom.getBuilding)
        } else if (Strings.isNotBlank(classroom.getBuilding.getName)) {
          builder.where("classroom.building.name like :buildingName")
          params.put("buildingName", "%" + classroom.getBuilding.getName + "%")
        }
      }
      if (Strings.isNotEmpty(classroom.getName)) {
        builder.where(Condition.like("classroom.name", classroom.getName))
      }
      if (Strings.isNotEmpty(classroom.getCode)) {
        builder.where(Condition.like("classroom.code", classroom.getCode))
      }
      if (null != classroom.getType) {
        builder.where("classroom.type =:type")
        params.put("type", classroom.getType)
      }
      if (classroom.getFloor != 0) {
        builder.where("(classroom.floor = :floor)")
        params.put("floor", classroom.getFloor)
      }
    }
    if (courseTimes.length > 0) {
      builder.where("exists (" + hql.toString + ")")
    }
    builder.params(params)
    builder
  }

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

  def getClassrooms(classroom: Classroom, departments: List[Department], limit: PageLimit): List[Classroom] = {
    val builder = OqlBuilder.from(classOf[Classroom], "classroom").where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)", 
      new java.util.Date())
      .limit(limit)
    if (!departments.isEmpty) {
      builder.where("classroom.building.department in (:departments)", departments)
    }
    if (null != classroom) {
      val campus = classroom.getCampus
      if (null != campus && campus.isPersisted) {
        builder.where("classroom.campus = :campus", campus)
      }
      if (null != classroom.getId) {
        builder.where("(classroom.id = :id)", classroom.getId)
      }
      if (Strings.isNotEmpty(classroom.getCode)) {
        builder.where(Condition.like("classroom.code", classroom.getCode))
      }
      if (Strings.isNotEmpty(classroom.getName)) {
        builder.where(Condition.like("classroom.name", classroom.getName))
      }
      val `type` = classroom.getType
      if (null != `type` && `type`.isPersisted) {
        builder.where("classroom.type = :type", `type`)
      }
      val building = classroom.getBuilding
      if (null != building && building.isPersisted) {
        builder.where("classroom.building=:building", building)
      }
    }
    entityDao.search(builder)
  }
}
