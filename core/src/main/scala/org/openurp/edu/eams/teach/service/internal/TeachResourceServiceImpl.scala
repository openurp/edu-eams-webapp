package org.openurp.edu.eams.teach.service.internal

import java.io.Serializable
import java.util.Collection
import java.util.Date
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.Page
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Classroom
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.eams.classroom.Occupancy
import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.eams.teach.lesson.ExamActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.CourseLimitUtils
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.base.Program
import org.openurp.edu.eams.teach.service.TeachResourceService
import org.openurp.edu.eams.util.DataRealmLimit

import scala.collection.JavaConversions._

class TeachResourceServiceImpl extends BaseServiceImpl with TeachResourceService {

  def isStdOccupied(time: TimeUnit, stdId: java.lang.Long): Boolean = false

  def isStdsOccupied(time: TimeUnit, stdIds: Collection[_]): Boolean = false

  def isStdsOccupied(time: TimeUnit, stdIds: Collection[_], expect: Lesson): Boolean = false

  def isRoomOccupied(time: TimeUnit, roomId: Serializable): Boolean = false

  def isCourseActivityRoomOccupied(activity: CourseActivity): Boolean = false

  def isTeacherOccupied(time: TimeUnit, teacherId: java.lang.Long): Boolean = false

  def isAdminclassOccupied(time: TimeUnit, adminClassId: java.lang.Long): Boolean = false

  def isAdminclassesOccupied(time: TimeUnit, adminClasses: Collection[_]): Boolean = false

  def getFreeRoomIn(roomIds: Collection[_], 
      times: Array[TimeUnit], 
      room: Classroom, 
      activityClass: Class[_]): Classroom = null

  def getFreeRoomsIn(roomIds: Collection[_], 
      times: Array[TimeUnit], 
      room: Classroom, 
      activityClass: Class[_]): Collection[_] = null

  def getFreeRoomsOf(query: OqlBuilder[Classroom], 
      params: Map[String, Any], 
      departs: List[Department], 
      unit: TimeUnit, 
      rooms: List[Classroom]): OqlBuilder[Classroom] = {
    query.where("not exists(select 1 from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom " + 
      "and (occupancy.time.year = :year) " + 
      "and (occupancy.time.weekState = :weekState) " + 
      "and (occupancy.time.weekday = :weekday) " + 
      "and (:startTime <= occupancy.time.endTime and :endTime > occupancy.time.startTime)" + 
      ")")
    query.where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)")
    query.join("classroom.departments", "depart")
    query.where("depart in (:departs)")
    query.select("distinct classroom")
    if (!rooms.isEmpty) {
      query.where("classroom not in (:rooms)")
    }
    params.put("year", unit.year)
    params.put("weekState", unit.weekState)
    params.put("weekday", unit.getWeekday)
    params.put("startTime", unit.getStartTime)
    params.put("endTime", unit.getEndTime)
    params.put("now", new Date())
    params.put("departs", departs)
    if (!rooms.isEmpty) {
      params.put("rooms", rooms)
    }
    query.params(params)
    null
  }

  def getFreeRoomsOf(departIds: Array[Long], 
      times: Array[TimeUnit], 
      pageNo: Int, 
      pageSize: Int, 
      order: Order): Collection[_] = {
    val query = OqlBuilder.from(classOf[Classroom], "room")
    query.where("room.building.department.id in (:departs)", departIds)
    query.where("not exists (select 1 from org.openurp.edu.eams.classroom.Occupancy occ where occ.time in (:times) and room = occ.room)", 
      times)
    query.limit(pageNo, pageSize)
    query.orderBy(order)
    entityDao.search(query)
  }

  def getFreeRoomsOf(departIds: Array[Long], 
      times: Array[TimeUnit], 
      room: Classroom, 
      activityClass: Class[_], 
      pageNo: Int, 
      pageSize: Int): Page = null

  def getFreeRoomsOf(departIds: Array[Long], 
      times: Array[TimeUnit], 
      roomOccupation: Occupancy, 
      pageNo: Int, 
      pageSize: Int): Page = null

  def getFreeTeachersOf(semester: Semester, 
      departments: List[Department], 
      times: Array[CourseTime], 
      teacher: Teacher, 
      replaceTeacher: Teacher, 
      pageLimit: PageLimit, 
      order: String): Collection[Teacher] = {
    val builder = OqlBuilder.from(classOf[Teacher], "teacher")
    if (CollectUtils.isNotEmpty(departments)) {
      builder.where("teacher.department in (:departments)", departments)
    }
    if (null != replaceTeacher) {
      builder.where("teacher!= :replaceTeacher", replaceTeacher)
    }
    if (null != teacher) {
      if (Strings.isNotEmpty(teacher.getCode)) {
        builder.where(Condition.like("teacher.code", teacher.getCode))
      }
      if (Strings.isNotEmpty(teacher.getName)) {
        builder.where(Condition.like("teacher.name", teacher.getName))
      }
      if (null != teacher.department) {
        builder.where("teacher.department = :deparment", teacher.department)
      }
    }
    val hql = new StringBuilder("not exists (from org.openurp.edu.teach.lesson.Lesson lesson join lesson.courseSchedule.activities activity " + 
      "join activity.teachers actTeacher where actTeacher=teacher ")
    if (semester != null) {
      hql.append("and lesson.semester.id = " + semester.getId + " ")
    }
    var occupy = ""
    for (i <- 0 until times.length) {
      occupy = "(bitand(activity.time.weekStateNum," + new java.lang.Long(times(i).getWeekStateNum) + 
        ")>0 and activity.time.weekday = " + 
        times(i).getWeekday + 
        " and " + 
        times(i).getStartTime + 
        " <= activity.time.endTime and " + 
        times(i).getEndTime + 
        " > activity.time.startTime)"
      if (i > 0) {
        hql.append(" or ")
      } else if (i == 0) {
        hql.append(" and (")
      }
      hql.append(occupy)
    }
    hql.append("))")
    if (times.length > 0) {
      builder.where(hql.toString)
    }
    builder.limit(pageLimit).orderBy(order)
    entityDao.search(builder)
  }

  def getFreeTeachersOf(departments: List[Department], 
      times: Array[CourseTime], 
      teacher: Teacher, 
      replaceTeacher: Teacher, 
      pageLimit: PageLimit, 
      order: String): Collection[Teacher] = {
    getFreeTeachersOf(null, departments, times, teacher, replaceTeacher, pageLimit, order)
  }

  def getFreeTeachersIn(teacherIds: Collection[_], 
      times: Array[TimeUnit], 
      teacher: Teacher, 
      activityClass: Class[_]): Collection[_] = null

  def getAdminclassActivities(adminClassId: Serializable, time: TimeUnit, activityClass: Class[_]): List[_] = {
    null
  }

  def getAdminclassActivities(adminclass: Adminclass, time: CourseTime, semester: Semester): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.where("activity.lesson.semester =:semester", semester)
    setTimeQuery(time, builder)
    val con = CourseLimitUtils.build(CourseLimitMetaEnum.ADMINCLASS.getMetaId, "lgi", adminclass.getId.toString)
    val params = con.getParams
    builder.where("exists(from activity.lesson.teachClass.limitGroups lg join lg.items as lgi where (lgi.operator='" + 
      Operator.EQUAL.name() + 
      "' or lgi.operator='" + 
      Operator.IN.name() + 
      "') and " + 
      con.getContent + 
      ")", params.get(0), params.get(1), params.get(2))
    entityDao.search(builder)
  }

  def getProgramActivities(program: Program, time: CourseTime, semester: Semester): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.where("activity.lesson.semester =:semester", semester)
    setTimeQuery(time, builder)
    val con = CourseLimitUtils.build(CourseLimitMetaEnum.PROGRAM.getMetaId, "lgi", program.getId.toString)
    val params = con.getParams
    builder.where("exists(from activity.lesson.teachClass.limitGroups lg join lg.items as lgi where (lgi.operator='" + 
      Operator.EQUAL.name() + 
      "' or lgi.operator='" + 
      Operator.IN.name() + 
      "') and " + 
      con.getContent + 
      ")", params.get(0), params.get(1), params.get(2))
    entityDao.search(builder)
  }

  protected def setTimeQuery(time: CourseTime, builder: OqlBuilder[CourseActivity]) {
    if (time != null) {
      if (null != time.getWeekday) {
        builder.where("activity.time.weekday =:weekday", time.getWeekday)
      }
      if (null != time.getEndUnit) {
        builder.where("activity.time.endUnit =:endUnit", time.getEndUnit)
      }
      if (null != time.getStartUnit) {
        builder.where("activity.time.startUnit =:startUnit", time.getStartUnit)
      }
      if (null != time.getWeekStateNum && 0 < time.getWeekStateNum) {
        builder.where("bitand(activity.time.weekStateNum," + time.getWeekStateNum + 
          ")>0")
      }
    }
  }

  def getTeacherActivities(teacher: Teacher, time: CourseTime, semester: Semester): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.join("activity.teachers", "teacher")
    builder.where("activity.lesson.semester =:semester", semester)
    setTimeQuery(time, builder)
    builder.where("teacher = :teacher", teacher)
    entityDao.search(builder)
  }

  def getRoomActivities(room: Classroom, time: CourseTime, semester: Semester): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.join("activity.rooms", "room")
    builder.where("activity.lesson.semester =:semester", semester)
    setTimeQuery(time, builder)
    builder.where("room = :room", room)
    entityDao.search(builder)
  }

  def getRoomActivities(room: Classroom, 
      time: CourseTime, 
      semester: Semester, 
      departments: List[Department], 
      project: Project): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.join("activity.rooms", "room")
    builder.where("activity.lesson.semester =:semester", semester)
    if (CollectUtils.isNotEmpty(departments)) {
      builder.where("activity.lesson.teachDepart in (:departments)", departments)
    }
    if (null != project) {
      builder.where("activity.lesson.project = :project", project)
    }
    setTimeQuery(time, builder)
    builder.where("room = :room", room)
    entityDao.search(builder)
  }

  def getStdActivities(student: Student, time: CourseTime, semester: Semester): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.join("activity.lesson.teachClass.courseTakes", "take")
    builder.where("activity.lesson.semester =:semester", semester)
    setTimeQuery(time, builder)
    builder.where("take.std = :student", student)
    entityDao.search(builder)
  }

  def getRoomActivities(roomId: Serializable, 
      time: TimeUnit, 
      activityClass: Class[_], 
      semester: Semester): List[_] = null

  def getStdActivities(stdId: java.lang.Long, 
      time: TimeUnit, 
      activityClass: Class[_], 
      semester: Semester): List[_] = null

  def getRoomOccupyInfos(roomId: java.lang.Integer, weekStateNum: java.lang.Long, year: java.lang.Integer): List[_] = {
    null
  }

  def getTeacherOccupyInfos(teacherId: java.lang.Long, weekStateNum: java.lang.Long, year: java.lang.Integer): List[_] = {
    null
  }

  def getAdminclassOccupyInfos(adminClassId: java.lang.Long, weekStateNum: java.lang.Long, year: java.lang.Integer): List[_] = {
    null
  }

  def getClassrooms(roomIds: Collection[_]): List[_] = null

  def getTeachers(teacherIds: Collection[_]): List[_] = null

  def getClassrooms(roomIdSeq: String): List[_] = null

  def getClassrooms(roomIds: Array[Integer]): List[_] = null

  def getRoomUtilizationsOfExam(semester: Semester, 
      examType: ExamType, 
      limit: DataRealmLimit, 
      ratio: java.lang.Float): Collection[_] = {
    val query = OqlBuilder.from(classOf[ExamActivity], "activity")
    query.where("activity.semester = :semester", semester)
    query.where("activity.examType = :examType", examType)
    val activities = entityDao.search(query)
    null
  }

  def getElectCountRoomUtilizationOfCourse(departments: List[Department], semester: Semester, ratio: java.lang.Float): Map[CourseActivity, Array[Any]] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
      .where("activity.lesson.semester=:semester", semester)
      .where("activity.lesson.teachDepart in (:depart)", departments)
    val activitys = entityDao.search(builder)
    val utilizations = CollectUtils.newHashMap()
    for (courseActivity <- activitys) {
      val rooms = courseActivity.getRooms
      var capacity = 0
      for (room <- rooms) {
        capacity += room.getCapacity
      }
      val objs = Array.ofDim[Any](2)
      objs(1) = capacity
      if (capacity != 0) {
        val ratioNow = courseActivity.getLesson.getTeachClass.getStdCount.toFloat / 
          capacity.toFloat
        if (ratioNow <= ratio) {
          objs(0) = ratioNow
          utilizations.put(courseActivity, objs)
        }
      } else {
        objs(0) = 0f
        utilizations.put(courseActivity, objs)
      }
    }
    utilizations
  }

  def getRoomUtilizationOfCourse(departments: List[Department], semester: Semester, ratio: java.lang.Float): Map[CourseActivity, Array[Any]] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
      .where("activity.lesson.semester=:semester", semester)
      .where("activity.lesson.teachDepart in (:depart)", departments)
    val activitys = entityDao.search(builder)
    val utilizations = CollectUtils.newHashMap()
    for (courseActivity <- activitys) {
      val rooms = courseActivity.getRooms
      var capacity = 0
      for (room <- rooms) {
        capacity += room.getCapacity
      }
      val objs = Array.ofDim[Any](2)
      objs(1) = capacity
      if (capacity != 0) {
        val ratioNow = courseActivity.getLesson.getTeachClass.getLimitCount.toFloat / 
          capacity.toFloat
        if (ratioNow <= ratio) {
          objs(0) = ratioNow
          utilizations.put(courseActivity, objs)
        }
      } else {
        objs(0) = 0f
        utilizations.put(courseActivity, objs)
      }
    }
    utilizations
  }

  def queryAdminclassByOccupyInfo(semester: Semester, 
      startWeek: java.lang.Integer, 
      endWeek: java.lang.Integer, 
      startWeekDay: java.lang.Integer, 
      endWeekDay: java.lang.Integer, 
      startUnit: java.lang.Integer, 
      endUnit: java.lang.Integer, 
      busy: Boolean, 
      dataRealm: DataRealm): Collection[Adminclass] = null

  def getTeacherActivities(teacherId: Serializable, 
      time: TimeUnit, 
      activityClass: Class[_], 
      semester: Semester): List[_] = null

  def getAdminclassActivities(adminClassId: Serializable, 
      time: TimeUnit, 
      activityClass: Class[_], 
      semester: Semester): List[_] = null

  def getFreeTeachersOf(departIds: Array[Long], 
      times: Array[TimeUnit], 
      teacher: Teacher, 
      activityClass: Class[_], 
      limit: PageLimit): Collection[_] = null

  def getTeacherPeriod(lesson: Lesson, teacher: Teacher): Int = {
    val courseActivities = lesson.getCourseSchedule.getActivities
    var period = 0
    for (courseActivity <- courseActivities if courseActivity.getTeachers.contains(teacher)) {
      val time = courseActivity.getTime
      period += (time.getEndUnit - time.getStartUnit + 1) * Strings.count(time.getWeekState, "1")
    }
    period
  }
}
