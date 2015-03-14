package org.openurp.edu.eams.teach.schedule.service

import java.util.Collection
import java.util.LinkedHashMap
import java.util.Map
import java.util.Set
import org.apache.commons.collections.MapUtils
import org.beangle.commons.collection.CollectUtils
import org.openurp.base.Room
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import BruteForceArrangeContext._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object BruteForceArrangeContext {

  class CommonConflictInfo[T] {

    @BeanProperty
    var object2conflictInfo: Map[T, String] = new LinkedHashMap[T, String]()

    def this(`object`: T, conflictInfo: String) {
      super()
      addConflictInfo(`object`, conflictInfo)
    }

    def this(objects: Collection[T], conflictInfo: String) {
      super()
      addConflictInfo(objects, conflictInfo)
    }

    def hasCollictInfo(): Boolean = {
      MapUtils.isNotEmpty(this.object2conflictInfo)
    }

    def addConflictInfo(`object`: T, conflictInfo: String) {
      object2conflictInfo.put(`object`, conflictInfo)
    }

    def addConflictInfo(objects: Collection[T], conflictInfo: String) {
      for (`object` <- objects) {
        addConflictInfo(`object`, conflictInfo)
      }
    }
  }
}

class BruteForceArrangeContext(@BeanProperty var lesson: Lesson, transientActivities: Collection[CourseActivity])
    {

  @BeanProperty
  var lessonOccupiedRooms: Set[Classroom] = CollectUtils.newHashSet()

  private var activities: Collection[CourseActivity] = transientActivities

  @BooleanBeanProperty
  var detectTake: Boolean = true

  @BooleanBeanProperty
  var detectTeacher: Boolean = true

  @BooleanBeanProperty
  var detectRoom: Boolean = true

  @BeanProperty
  var takeConflictInfo: CommonConflictInfo[CourseTake] = new CommonConflictInfo[CourseTake]()

  @BeanProperty
  var teacherConflictInfo: CommonConflictInfo[Teacher] = new CommonConflictInfo[Teacher]()

  @BooleanBeanProperty
  var success: Boolean = _

  @BooleanBeanProperty
  var noSuitableRoom: Boolean = false

  for (activity <- lesson.getCourseSchedule.getActivities; classroom <- activity.getRooms) {
    lessonOccupiedRooms.add(classroom)
  }

  def hasUnResolvableConflict(): Boolean = {
    this.takeConflictInfo.hasCollictInfo() || this.teacherConflictInfo.hasCollictInfo()
  }

  def detectTake(): BruteForceArrangeContext = {
    this.detectTake = true
    this
  }

  def dontDetectTake(): BruteForceArrangeContext = {
    this.detectTake = false
    this
  }

  def detectTeacher(): BruteForceArrangeContext = {
    this.detectTeacher = true
    this
  }

  def dontDetectTeacher(): BruteForceArrangeContext = {
    this.detectTeacher = false
    this
  }

  def detectRoom(): BruteForceArrangeContext = {
    this.detectRoom = true
    this
  }

  def dontDetectRoom(): BruteForceArrangeContext = {
    this.detectRoom = false
    this
  }

  def getTransientActivities(): Collection[CourseActivity] = activities

  def buildRoomsConflictInfo(): CommonConflictInfo[Classroom] = new CommonConflictInfo[Classroom]()

  def noSuitableRoom() {
    failed()
    this.noSuitableRoom = true
  }

  def succeed() {
    this.success = true
  }

  def failed() {
    this.success = false
  }

  def isFailed(): Boolean = !isSuccess
}


