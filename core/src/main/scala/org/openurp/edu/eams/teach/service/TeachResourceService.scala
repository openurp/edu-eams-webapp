package org.openurp.edu.eams.teach.service

import java.io.Serializable
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.Page
import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.base.Semester
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.base.Program
import org.openurp.edu.eams.util.DataRealmLimit
import org.openurp.edu.teach.code.ExamType
import org.openurp.lg.room.Occupancy
import org.openurp.base.SemesterWeekTime
import org.openurp.base.User

trait TeachResourceService {

  def isStdOccupied(time: YearWeekTime, stdId: java.lang.Long): Boolean

  def isStdsOccupied(time: YearWeekTime, stdIds: Iterable[_]): Boolean

  def isStdsOccupied(time: YearWeekTime, stdIds: Iterable[_], expect: Lesson): Boolean

  def isRoomOccupied(time: YearWeekTime, roomId: Serializable): Boolean

  def isCourseActivityRoomOccupied(activity: CourseActivity): Boolean

  def isTeacherOccupied(time: YearWeekTime, teacherId: java.lang.Long): Boolean

  def isAdminclassOccupied(time: YearWeekTime, adminClassId: java.lang.Long): Boolean

  def isAdminclassesOccupied(time: YearWeekTime, adminClasses: Iterable[_]): Boolean

  def getFreeRoomIn(roomIds: Iterable[_],
    times: Array[YearWeekTime],
    room: Room,
    activityClass: Class[_]): Room

  def getFreeRoomsIn(roomIds: Iterable[_],
    times: Array[YearWeekTime],
    room: Room,
    activityClass: Class[_]): Iterable[_]

  def getFreeRoomsOf(query: OqlBuilder[Room],
    params: Map[String, Any],
    departs: Seq[Department],
    unit: YearWeekTime,
    rooms: Seq[Room]): OqlBuilder[Room]

  def getFreeRoomsOf(departIds: Array[Long],
    times: Array[YearWeekTime],
    pageNo: Int,
    pageSize: Int,
    order: Order): Iterable[_]

  def getFreeRoomsOf(departIds: Array[Long],
    times: Array[YearWeekTime],
    room: Room,
    activityClass: Class[_],
    pageNo: Int,
    pageSize: Int): Page[_]

  def getFreeRoomsOf(departIds: Array[Long],
    times: Array[YearWeekTime],
    roomOccupation: Occupancy,
    pageNo: Int,
    pageSize: Int): Page[_]

  def getFreeTeachersOf(departIds: Array[Long],
    times: Array[YearWeekTime],
    teacher: Teacher,
    activityClass: Class[_],
    limit: PageLimit): Iterable[_]

  def getFreeTeachersIn(teacherIds: Iterable[_],
    times: Array[YearWeekTime],
    teacher: Teacher,
    activityClass: Class[_]): Iterable[_]

  def getTeacherActivities(teacherId: Serializable,
    time: YearWeekTime,
    activityClass: Class[_],
    semester: Semester): Seq[_]

  def getAdminclassActivities(adminClassId: Serializable, time: YearWeekTime, activityClass: Class[_]): Seq[_]

  def getAdminclassActivities(adminClassId: Serializable,
    time: YearWeekTime,
    activityClass: Class[_],
    semester: Semester): Seq[_]

  def getRoomActivities(roomId: Serializable,
    time: YearWeekTime,
    activityClass: Class[_],
    semester: Semester): Seq[_]

  def getStdActivities(stdId: java.lang.Long,
    time: YearWeekTime,
    activityClass: Class[_],
    semester: Semester): Seq[_]

  def getRoomOccupyInfos(roomId: java.lang.Integer, weekStateNum: java.lang.Long, year: java.lang.Integer): Seq[_]

  def getTeacherOccupyInfos(teacherId: java.lang.Long, weekStateNum: java.lang.Long, year: java.lang.Integer): Seq[_]

  def getAdminclassOccupyInfos(adminClassId: java.lang.Long, weekStateNum: java.lang.Long, year: java.lang.Integer): Seq[_]

  def getRooms(roomIds: Iterable[_]): Seq[_]

  def getTeachers(teacherIds: Iterable[_]): Seq[_]

  def getRooms(roomIdSeq: String): Seq[_]

  def getRooms(roomIds: Array[Integer]): Seq[_]

  def getRoomUtilizationsOfExam(semester: Semester,
    examType: ExamType,
    limit: DataRealmLimit,
    ratio: java.lang.Float): Iterable[_]

  def getRoomUtilizationOfCourse(departments: Seq[Department], semester: Semester, ratio: java.lang.Float): collection.Map[CourseActivity, Array[Any]]

  def getElectCountRoomUtilizationOfCourse(departments: Seq[Department], semester: Semester, ratio: java.lang.Float): collection.Map[CourseActivity, Array[Any]]

  def queryAdminclassByOccupyInfo(semester: Semester,
    startWeek: java.lang.Integer,
    endWeek: java.lang.Integer,
    startWeekDay: java.lang.Integer,
    endWeekDay: java.lang.Integer,
    startUnit: java.lang.Integer,
    endUnit: java.lang.Integer,
    busy: Boolean,
    dataRealm: DataRealm): Iterable[Adminclass]

  def getAdminclassActivities(adminclass: Adminclass, time: SemesterWeekTime): Seq[CourseActivity]

  def getTeacherActivities(teacher: Teacher, time: SemesterWeekTime): Seq[CourseActivity]

  def getRoomActivities(room: Room, time: SemesterWeekTime): Seq[CourseActivity]

  def getRoomActivities(room: Room,
    time: SemesterWeekTime,
    departments: Seq[Department],
    project: Project): Seq[CourseActivity]

  def getStdActivities(student: Student, time: SemesterWeekTime): Seq[CourseActivity]

  def getFreeTeachersOf(semester: Semester,
    departments: Seq[Department],
    times: Array[YearWeekTime],
    user: User,
    replaceTeacher:  User,
    pageLimit: PageLimit,
    order: String): Iterable[User]

  def getFreeTeachersOf(departments: Seq[Department],
    times: Array[YearWeekTime],
    teacher:  User,
    replaceTeacher:  User,
    pageLimit: PageLimit,
    order: String): Iterable[User]

  def getTeacherPeriod(lesson: Lesson, teacher: Teacher): Int

  def getProgramActivities(program: Program, time: SemesterWeekTime): Seq[CourseActivity]
}
