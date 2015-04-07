package org.openurp.edu.eams.teach.schedule.service


import java.util.LinkedHashMap


import org.beangle.commons.collection.Collections
import org.openurp.base.Room
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import BruteForceArrangeContext._




object BruteForceArrangeContext {

  class CommonConflictInfo[T] {

    
    var object2conflictInfo: collection.mutable.Map[T, String] = Collections.newMap[T, String]

    def this(`object`: T, conflictInfo: String) {
      this()
      addConflictInfo(`object`, conflictInfo)
    }

    def this(objects: Iterable[T], conflictInfo: String) {
      this()
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

  
  var lessonOccupiedRooms: collection.mutable.Set[Room] = Collections.newSet[Room]

  var activities: Iterable[CourseActivity] = transientActivities

  
  var detectTake: Boolean = true

  
  var detectTeacher: Boolean = true

  
  var detectRoom: Boolean = true

  
  var takeConflictInfo: CommonConflictInfo[CourseTake] = new CommonConflictInfo[CourseTake]()

  
  var teacherConflictInfo: CommonConflictInfo[Teacher] = new CommonConflictInfo[Teacher]()

  
  var success: Boolean = true

  
  var noSuitableRoom: Boolean = false

  for (activity <- lesson.schedule.activities; classroom <- activity.rooms) {
    lessonOccupiedRooms.add(classroom)
  }

  def hasUnResolvableConflict(): Boolean = {
    this.takeConflictInfo.hasCollictInfo() || this.teacherConflictInfo.hasCollictInfo()
  }


  def getTransientActivities(): Iterable[CourseActivity] = activities

  def buildRoomsConflictInfo(): CommonConflictInfo[Room] = new CommonConflictInfo[Room]()

}


