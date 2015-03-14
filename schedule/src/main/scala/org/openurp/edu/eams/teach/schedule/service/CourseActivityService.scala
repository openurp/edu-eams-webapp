package org.openurp.edu.eams.teach.schedule.service

import java.sql.Date
import java.util.Collection
import java.util.List
import java.util.Set
import org.beangle.commons.entity.Entity
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.base.CourseUnit
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.TimeSetting
import org.openurp.edu.eams.classroom.Occupancy
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.schedule.model.CollisionInfo
import org.openurp.edu.eams.teach.schedule.model.CollisionResource.ResourceType

import scala.collection.JavaConversions._

trait CourseActivityService {

  def saveActivities(lessons: Collection[Lesson]): Unit

  def removeActivities(lessons: Collection[Lesson]): Unit

  def removeActivities(lessonIds: Array[Long], semester: Semester): Unit

  def getCourseUnits(lessonId: java.lang.Long, date: Date): List[CourseUnit]

  def collisionTakes(lesson: Lesson, activities: Collection[CourseActivity]): Collection[CourseTake]

  def getCourseTakes(stdId: java.lang.Long, time: CourseTime, semester: Semester): List[CourseTake]

  def saveOrUpdateActivity(lesson: Lesson, 
      occupancies: Set[Occupancy], 
      alterationBeforeMsg: String, 
      canToMessage: java.lang.Boolean, 
      user: User, 
      remoteAddr: String): Unit

  def isCourseActivityRoomOccupied(activity: CourseActivity): Boolean

  def shift(lesson: Lesson, 
      offset: Int, 
      canToMessage: java.lang.Boolean, 
      user: User): Unit

  def shift(activities: Collection[CourseActivity], 
      fromYear: Int, 
      fromWeekStart: Int, 
      fromWeekEnd: Int, 
      toWeekStart: Int, 
      toWeekEnd: Int, 
      timeSetting: TimeSetting, 
      to: Semester): Collection[Entity[Long]]

  def mergeActivites(tobeMerged: List[CourseActivity]): List[CourseActivity]

  def detectCollision[T](semester: Semester, `type`: ResourceType, timeSetting: TimeSetting): Collection[CollisionInfo]

  def detectCollision[T](semester: Semester, 
      `type`: ResourceType, 
      timeSetting: TimeSetting, 
      lessonId: java.lang.Long): Collection[CollisionInfo]

  def saveOrUpdateActivityWithnoAlterInfos(lesson: Lesson, 
      occupancies: Set[Occupancy], 
      alterationBeforeMsg: String, 
      canToMessage: java.lang.Boolean, 
      user: User, 
      remoteAddr: String): Unit

  def isCourseActivityTeacherOccupied(activity: CourseActivity): Boolean
}
