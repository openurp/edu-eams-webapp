package org.openurp.edu.eams.teach.service.internal

import java.io.Serializable

import java.util.Date



import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.Page
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.eams.classroom.Occupancy
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operator
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.LessonLimitUtils
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaEnum
import org.openurp.edu.base.Program
import org.openurp.edu.eams.teach.service.TeachResourceService
import org.openurp.edu.eams.util.DataRealmLimit



class TeachResourceServiceImpl extends BaseServiceImpl with TeachResourceService {

  def isStdOccupied(time: YearWeekTime, stdId: java.lang.Long): Boolean = false

  def isStdsOccupied(time: YearWeekTime, stdIds: Iterable[_]): Boolean = false

  def isStdsOccupied(time: YearWeekTime, stdIds: Iterable[_], expect: Lesson): Boolean = false

  def isRoomOccupied(time: YearWeekTime, roomId: Serializable): Boolean = false

  def isCourseActivityRoomOccupied(activity: CourseActivity): Boolean = false

  def isTeacherOccupied(time: YearWeekTime, teacherId: java.lang.Long): Boolean = false

  def isAdminclassOccupied(time: YearWeekTime, adminClassId: java.lang.Long): Boolean = false

  def isAdminclassesOccupied(time: YearWeekTime, adminClasses: Iterable[_]): Boolean = false

  def getFreeRoomIn(roomIds: Iterable[_], 
      times: Array[YearWeekTime], 
      room: Room, 
      activityClass: Class[_]): Room = null

  def getFreeRoomsIn(roomIds: Iterable[_], 
      times: Array[YearWeekTime], 
      room: Room, 
      activityClass: Class[_]): Iterable[_] = null

  def getFreeRoomsOf(query: OqlBuilder[Room], 
      params: Map[String, Any], 
      departs: List[Department], 
      unit: YearWeekTime, 
      rooms: List[Room]): OqlBuilder[Room] = {
    query.where("not exists(select 1 from org.openurp.edu.eams.classroom.Occupancy occupancy where occupancy.room = classroom " + 
      "and (occupancy.time.year = :year) " + 
      "and (occupancy.time.state = :weekState) " + 
      "and (occupancy.time.day = :weekday) " + 
      "and (:startTime <= occupancy.time.end and :endTime > occupancy.time.start)" + 
      ")")
    query.where("classroom.effectiveAt <= :now and (classroom.invalidAt is null or classroom.invalidAt >= :now)")
    query.join("classroom.departments", "depart")
    query.where("depart in (:departs)")
    query.select("distinct classroom")
    if (!rooms.isEmpty) {
      query.where("classroom not in (:rooms)")
    }
    params.put("year", unit.year)
    params.put("weekState", unit.state)
    params.put("weekday", unit.day)
    params.put("startTime", unit.start)
    params.put("endTime", unit.end)
    params.put("now", new Date())
    params.put("departs", departs)
    if (!rooms.isEmpty) {
      params.put("rooms", rooms)
    }
    query.params(params)
    null
  }

  def getFreeRoomsOf(departIds: Array[Long], 
      times: Array[YearWeekTime], 
      pageNo: Int, 
      pageSize: Int, 
      order: Order): Iterable[_] = {
    val query = OqlBuilder.from(classOf[Room], "room")
    query.where("room.building.department.id in (:departs)", departIds)
    query.where("not exists (select 1 from org.openurp.edu.eams.classroom.Occupancy occ where occ.time in (:times) and room = occ.room)", 
      times)
    query.limit(pageNo, pageSize)
    query.orderBy(order)
    entityDao.search(query)
  }

  def getFreeRoomsOf(departIds: Array[Long], 
      times: Array[YearWeekTime], 
      room: Room, 
      activityClass: Class[_], 
      pageNo: Int, 
      pageSize: Int): Page = null

  def getFreeRoomsOf(departIds: Array[Long], 
      times: Array[YearWeekTime], 
      roomOccupation: Occupancy, 
      pageNo: Int, 
      pageSize: Int): Page = null

  def getFreeTeachersOf(semester: Semester, 
      departments: List[Department], 
      times: Array[CourseTime], 
      teacher: Teacher, 
      replaceTeacher: Teacher, 
      pageLimit: PageLimit, 
      order: String): Iterable[Teacher] = {
    val builder = OqlBuilder.from(classOf[Teacher], "teacher")
    if (Collections.isNotEmpty(departments)) {
      builder.where("teacher.department in (:departments)", departments)
    }
    if (null != replaceTeacher) {
      builder.where("teacher!= :replaceTeacher", replaceTeacher)
    }
    if (null != teacher) {
      if (Strings.isNotEmpty(teacher.code)) {
        builder.where(Condition.like("teacher.code", teacher.code))
      }
      if (Strings.isNotEmpty(teacher.name)) {
        builder.where(Condition.like("teacher.name", teacher.name))
      }
      if (null != teacher.department) {
        builder.where("teacher.department = :deparment", teacher.department)
      }
    }
    val hql = new StringBuilder("not exists (from org.openurp.edu.teach.lesson.Lesson lesson join lesson.schedule.activities activity " + 
      "join activity.teachers actTeacher where actTeacher=teacher ")
    if (semester != null) {
      hql.append("and lesson.semester.id = " + semester.id + " ")
    }
    var occupy = ""
    for (i <- 0 until times.length) {
      occupy = "(bitand(activity.time.state," + new java.lang.Long(times(i).state) + 
        ")>0 and activity.time.day = " + 
        times(i).day + 
        " and " + 
        times(i).start + 
        " <= activity.time.end and " + 
        times(i).end + 
        " > activity.time.start)"
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
      order: String): Iterable[Teacher] = {
    getFreeTeachersOf(null, departments, times, teacher, replaceTeacher, pageLimit, order)
  }

  def getFreeTeachersIn(teacherIds: Iterable[_], 
      times: Array[YearWeekTime], 
      teacher: Teacher, 
      activityClass: Class[_]): Iterable[_] = null

  def getAdminclassActivities(adminClassId: Serializable, time: YearWeekTime, activityClass: Class[_]): List[_] = {
    null
  }

  def getAdminclassActivities(adminclass: Adminclass, time: CourseTime, semester: Semester): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.where("activity.lesson.semester =:semester", semester)
    setTimeQuery(time, builder)
    val con = LessonLimitUtils.build(LessonLimitMeta.Adminclass.id, "lgi", adminclass.id.toString)
    val params = con.params
    builder.where("exists(from activity.lesson.teachClass.limitGroups lg join lg.items as lgi where (lgi.operator='" + 
      Operator.Equals.name() + 
      "' or lgi.operator='" + 
      Operator.IN.name() + 
      "') and " + 
      con.content + 
      ")", params.get(0), params.get(1), params.get(2))
    entityDao.search(builder)
  }

  def getProgramActivities(program: Program, time: CourseTime, semester: Semester): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.where("activity.lesson.semester =:semester", semester)
    setTimeQuery(time, builder)
    val con = LessonLimitUtils.build(LessonLimitMeta.Program.id, "lgi", program.id.toString)
    val params = con.params
    builder.where("exists(from activity.lesson.teachClass.limitGroups lg join lg.items as lgi where (lgi.operator='" + 
      Operator.Equals.name() + 
      "' or lgi.operator='" + 
      Operator.IN.name() + 
      "') and " + 
      con.content + 
      ")", params.get(0), params.get(1), params.get(2))
    entityDao.search(builder)
  }

  protected def setTimeQuery(time: CourseTime, builder: OqlBuilder[CourseActivity]) {
    if (time != null) {
      if (null != time.day) {
        builder.where("activity.time.day =:weekday", time.day)
      }
      if (null != time.endUnit) {
        builder.where("activity.time.endUnit =:endUnit", time.endUnit)
      }
      if (null != time.startUnit) {
        builder.where("activity.time.startUnit =:startUnit", time.startUnit)
      }
      if (null != time.state && 0 < time.state) {
        builder.where("bitand(activity.time.state," + time.state + 
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

  def getRoomActivities(room: Room, time: CourseTime, semester: Semester): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.join("activity.rooms", "room")
    builder.where("activity.lesson.semester =:semester", semester)
    setTimeQuery(time, builder)
    builder.where("room = :room", room)
    entityDao.search(builder)
  }

  def getRoomActivities(room: Room, 
      time: CourseTime, 
      semester: Semester, 
      departments: List[Department], 
      project: Project): List[CourseActivity] = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.join("activity.rooms", "room")
    builder.where("activity.lesson.semester =:semester", semester)
    if (Collections.isNotEmpty(departments)) {
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
      time: YearWeekTime, 
      activityClass: Class[_], 
      semester: Semester): List[_] = null

  def getStdActivities(stdId: java.lang.Long, 
      time: YearWeekTime, 
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

  def getRooms(roomIds: Iterable[_]): List[_] = null

  def getTeachers(teacherIds: Iterable[_]): List[_] = null

  def getRooms(roomIdSeq: String): List[_] = null

  def getRooms(roomIds: Array[Integer]): List[_] = null

  def getRoomUtilizationsOfExam(semester: Semester, 
      examType: ExamType, 
      limit: DataRealmLimit, 
      ratio: java.lang.Float): Iterable[_] = {
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
    val utilizations = Collections.newMap[Any]
    for (courseActivity <- activitys) {
      val rooms = courseActivity.rooms
      var capacity = 0
      for (room <- rooms) {
        capacity += room.capacity
      }
      val objs = Array.ofDim[Any](2)
      objs(1) = capacity
      if (capacity != 0) {
        val ratioNow = courseActivity.lesson.teachClass.stdCount.toFloat / 
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
    val utilizations = Collections.newMap[Any]
    for (courseActivity <- activitys) {
      val rooms = courseActivity.rooms
      var capacity = 0
      for (room <- rooms) {
        capacity += room.capacity
      }
      val objs = Array.ofDim[Any](2)
      objs(1) = capacity
      if (capacity != 0) {
        val ratioNow = courseActivity.lesson.teachClass.limitCount.toFloat / 
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
      dataRealm: DataRealm): Iterable[Adminclass] = null

  def getTeacherActivities(teacherId: Serializable, 
      time: YearWeekTime, 
      activityClass: Class[_], 
      semester: Semester): List[_] = null

  def getAdminclassActivities(adminClassId: Serializable, 
      time: YearWeekTime, 
      activityClass: Class[_], 
      semester: Semester): List[_] = null

  def getFreeTeachersOf(departIds: Array[Long], 
      times: Array[YearWeekTime], 
      teacher: Teacher, 
      activityClass: Class[_], 
      limit: PageLimit): Iterable[_] = null

  def getTeacherPeriod(lesson: Lesson, teacher: Teacher): Int = {
    val courseActivities = lesson.schedule.activities
    var period = 0
    for (courseActivity <- courseActivities if courseActivity.teachers.contains(teacher)) {
      val time = courseActivity.getTime
      period += (time.endUnit - time.startUnit + 1) * Strings.count(time.state, "1")
    }
    period
  }
}
