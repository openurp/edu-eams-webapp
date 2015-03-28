package org.openurp.edu.eams.teach.schedule.service


import java.util.LinkedHashMap


import org.apache.commons.collections.MapUtils
import org.beangle.commons.collection.Collections
import org.openurp.base.Room
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import BruteForceArrangeContext._




object BruteForceArrangeContext {

  class CommonConflictInfo[T] {

    
    var object2conflictInfo: Map[T, String] = new LinkedHashMap[T, String]()

    def this(`object`: T, conflictInfo: String) {
      super()
      addConflictInfo(`object`, conflictInfo)
    }

    def this(objects: Iterable[T], conflictInfo: String) {
      super()
      addConflictInfo(objects, conflictInfo)
    }

    def hasCollictInfo(): Boolean = {
      MapUtils.isNotEmpty(this.object2conflictInfo)
    }

    def addConflictInfo(`object`: T, conflictInfo: String) {
      object2conflictInfo.put(`object`, conflictInfo)
    }

    def addConflictInfo(objects: Iterable[T], conflictInfo: String) {
      for (`object` <- objects) {
        addConflictInfo(`object`, conflictInfo)
      }
    }
  }
}

class BruteForceArrangeContext( var lesson: Lesson, transientActivities: Iterable[CourseActivity])
    {

  
  var lessonOccupiedRooms: Set[Room] = Collections.newSet[Any]

  private var activities: Iterable[CourseActivity] = transientActivities

  
  var detectTake: Boolean = true

  
  var detectTeacher: Boolean = true

  
  var detectRoom: Boolean = true

  
  var takeConflictInfo: CommonConflictInfo[CourseTake] = new CommonConflictInfo[CourseTake]()

  
  var teacherConflictInfo: CommonConflictInfo[Teacher] = new CommonConflictInfo[Teacher]()

  
  var success: Boolean = _

  
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

  def getTransientActivities(): Iterable[CourseActivity] = activities

  def buildRoomsConflictInfo(): CommonConflictInfo[Room] = new CommonConflictInfo[Room]()

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


