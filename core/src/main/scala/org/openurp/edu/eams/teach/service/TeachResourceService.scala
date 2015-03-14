package org.openurp.edu.eams.teach.service

import java.io.Serializable
import java.util.Collection
import java.util.List
import java.util.Map
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.Page
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.query.builder.OqlBuilder
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
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.base.Program
import org.openurp.edu.eams.util.DataRealmLimit

import scala.collection.JavaConversions._

trait TeachResourceService {

  def isStdOccupied(time: TimeUnit, stdId: java.lang.Long): Boolean

  def isStdsOccupied(time: TimeUnit, stdIds: Collection[_]): Boolean

  def isStdsOccupied(time: TimeUnit, stdIds: Collection[_], expect: Lesson): Boolean

  def isRoomOccupied(time: TimeUnit, roomId: Serializable): Boolean

  def isCourseActivityRoomOccupied(activity: CourseActivity): Boolean

  def isTeacherOccupied(time: TimeUnit, teacherId: java.lang.Long): Boolean

  def isAdminclassOccupied(time: TimeUnit, adminClassId: java.lang.Long): Boolean

  def isAdminclassesOccupied(time: TimeUnit, adminClasses: Collection[_]): Boolean

  def getFreeRoomIn(roomIds: Collection[_], 
      times: Array[TimeUnit], 
      room: Classroom, 
      activityClass: Class[_]): Classroom

  def getFreeRoomsIn(roomIds: Collection[_], 
      times: Array[TimeUnit], 
      room: Classroom, 
      activityClass: Class[_]): Collection[_]

  def getFreeRoomsOf(query: OqlBuilder[Classroom], 
      params: Map[String, Any], 
      departs: List[Department], 
      unit: TimeUnit, 
      rooms: List[Classroom]): OqlBuilder[Classroom]

  def getFreeRoomsOf(departIds: Array[Long], 
      times: Array[TimeUnit], 
      pageNo: Int, 
      pageSize: Int, 
      order: Order): Collection[_]

  def getFreeRoomsOf(departIds: Array[Long], 
      times: Array[TimeUnit], 
      room: Classroom, 
      activityClass: Class[_], 
      pageNo: Int, 
      pageSize: Int): Page

  def getFreeRoomsOf(departIds: Array[Long], 
      times: Array[TimeUnit], 
      roomOccupation: Occupancy, 
      pageNo: Int, 
      pageSize: Int): Page

  def getFreeTeachersOf(departIds: Array[Long], 
      times: Array[TimeUnit], 
      teacher: Teacher, 
      activityClass: Class[_], 
      limit: PageLimit): Collection[_]

  def getFreeTeachersIn(teacherIds: Collection[_], 
      times: Array[TimeUnit], 
      teacher: Teacher, 
      activityClass: Class[_]): Collection[_]

  def getTeacherActivities(teacherId: Serializable, 
      time: TimeUnit, 
      activityClass: Class[_], 
      semester: Semester): List[_]

  def getAdminclassActivities(adminClassId: Serializable, time: TimeUnit, activityClass: Class[_]): List[_]

  def getAdminclassActivities(adminClassId: Serializable, 
      time: TimeUnit, 
      activityClass: Class[_], 
      semester: Semester): List[_]

  def getRoomActivities(roomId: Serializable, 
      time: TimeUnit, 
      activityClass: Class[_], 
      semester: Semester): List[_]

  def getStdActivities(stdId: java.lang.Long, 
      time: TimeUnit, 
      activityClass: Class[_], 
      semester: Semester): List[_]

  def getRoomOccupyInfos(roomId: java.lang.Integer, weekStateNum: java.lang.Long, year: java.lang.Integer): List[_]

  def getTeacherOccupyInfos(teacherId: java.lang.Long, weekStateNum: java.lang.Long, year: java.lang.Integer): List[_]

  def getAdminclassOccupyInfos(adminClassId: java.lang.Long, weekStateNum: java.lang.Long, year: java.lang.Integer): List[_]

  def getClassrooms(roomIds: Collection[_]): List[_]

  def getTeachers(teacherIds: Collection[_]): List[_]

  def getClassrooms(roomIdSeq: String): List[_]

  def getClassrooms(roomIds: Array[Integer]): List[_]

  def getRoomUtilizationsOfExam(semester: Semester, 
      examType: ExamType, 
      limit: DataRealmLimit, 
      ratio: java.lang.Float): Collection[_]

  def getRoomUtilizationOfCourse(departments: List[Department], semester: Semester, ratio: java.lang.Float): Map[CourseActivity, Array[Any]]

  def getElectCountRoomUtilizationOfCourse(departments: List[Department], semester: Semester, ratio: java.lang.Float): Map[CourseActivity, Array[Any]]

  def queryAdminclassByOccupyInfo(semester: Semester, 
      startWeek: java.lang.Integer, 
      endWeek: java.lang.Integer, 
      startWeekDay: java.lang.Integer, 
      endWeekDay: java.lang.Integer, 
      startUnit: java.lang.Integer, 
      endUnit: java.lang.Integer, 
      busy: Boolean, 
      dataRealm: DataRealm): Collection[Adminclass]

  def getAdminclassActivities(adminclass: Adminclass, time: CourseTime, semester: Semester): List[CourseActivity]

  def getTeacherActivities(teacher: Teacher, time: CourseTime, semester: Semester): List[CourseActivity]

  def getRoomActivities(room: Classroom, time: CourseTime, semester: Semester): List[CourseActivity]

  def getRoomActivities(room: Classroom, 
      time: CourseTime, 
      semester: Semester, 
      departments: List[Department], 
      project: Project): List[CourseActivity]

  def getStdActivities(student: Student, time: CourseTime, semester: Semester): List[CourseActivity]

  def getFreeTeachersOf(semester: Semester, 
      departments: List[Department], 
      times: Array[CourseTime], 
      teacher: Teacher, 
      replaceTeacher: Teacher, 
      pageLimit: PageLimit, 
      order: String): Collection[Teacher]

  def getFreeTeachersOf(departments: List[Department], 
      times: Array[CourseTime], 
      teacher: Teacher, 
      replaceTeacher: Teacher, 
      pageLimit: PageLimit, 
      order: String): Collection[Teacher]

  def getTeacherPeriod(lesson: Lesson, teacher: Teacher): Int

  def getProgramActivities(program: Program, time: CourseTime, semester: Semester): List[CourseActivity]
}
