package org.openurp.edu.eams.teach.schedule.service

import java.sql.Date



import org.beangle.data.model.Entity
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.base.CourseUnit
import org.openurp.base.Semester
import org.openurp.base.TimeSetting
import org.openurp.edu.eams.classroom.Occupancy
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.schedule.model.CollisionInfo
import org.openurp.edu.eams.teach.schedule.model.CollisionResource.ResourceType



trait CourseActivityService {

  def saveActivities(lessons: Iterable[Lesson]): Unit

  def removeActivities(lessons: Iterable[Lesson]): Unit

  def removeActivities(lessonIds: Array[Long], semester: Semester): Unit

  def getCourseUnits(lessonId: java.lang.Long, date: Date): List[CourseUnit]

  def collisionTakes(lesson: Lesson, activities: Iterable[CourseActivity]): Iterable[CourseTake]

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

  def shift(activities: Iterable[CourseActivity], 
      fromYear: Int, 
      fromWeekStart: Int, 
      fromWeekEnd: Int, 
      toWeekStart: Int, 
      toWeekEnd: Int, 
      timeSetting: TimeSetting, 
      to: Semester): Iterable[Entity[Long]]

  def mergeActivites(tobeMerged: List[CourseActivity]): List[CourseActivity]

  def detectCollision[T](semester: Semester, `type`: ResourceType, timeSetting: TimeSetting): Iterable[CollisionInfo]

  def detectCollision[T](semester: Semester, 
      `type`: ResourceType, 
      timeSetting: TimeSetting, 
      lessonId: java.lang.Long): Iterable[CollisionInfo]

  def saveOrUpdateActivityWithnoAlterInfos(lesson: Lesson, 
      occupancies: Set[Occupancy], 
      alterationBeforeMsg: String, 
      canToMessage: java.lang.Boolean, 
      user: User, 
      remoteAddr: String): Unit

  def isCourseActivityTeacherOccupied(activity: CourseActivity): Boolean
}
