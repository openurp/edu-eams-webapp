package org.openurp.edu.eams.teach.schedule.service

import java.util.List
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.eams.teach.lesson.CourseTime

import scala.collection.JavaConversions._

trait ScheduleRoomService {

  def getFreeRoomsOf(departments: List[Department], courseTimes: Array[CourseTime], activity: CourseActivity): OqlBuilder[Classroom]

  def getOccupancyRoomsOf(departments: List[Department], courseTimes: Array[CourseTime], activity: CourseActivity): OqlBuilder[Classroom]

  def getFreeRoomsOfConditions(units: Array[TimeUnit]): OqlBuilder[Classroom]

  def getClassrooms(classroom: Classroom, departments: List[Department], pageLimit: PageLimit): List[Classroom]
}
