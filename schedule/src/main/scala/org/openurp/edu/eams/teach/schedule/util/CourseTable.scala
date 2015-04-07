package org.openurp.edu.eams.teach.schedule.util




import org.beangle.commons.collection.Collections
import org.beangle.data.model.Entity
import org.openurp.base.Room
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.base.Program
import org.openurp.edu.eams.teach.schedule.web.action.CourseTableAction
import CourseTable._



object CourseTable {

  val CLASS = "class"

  val STD = "std"

  val TEACHER = "teacher"

  val ROOM = "room"

  val PROGRAM = "program"

  val resourceClass = Collections.newMap[Any,Any]

  resourceClass.put(CLASS, classOf[Adminclass])

  resourceClass.put(STD, classOf[Student])

  resourceClass.put(TEACHER, classOf[Teacher])

  resourceClass.put(ROOM, classOf[Room])

  resourceClass.put(PROGRAM, classOf[Program])

  def getResourceClass[T](kind: String): Class[T] = {
    val clazz = resourceClass.get(kind).asInstanceOf[Class[T]]
    if (null != clazz) {
      clazz
    } else {
      throw new RuntimeException("not supported Resource type:" + kind)
    }
  }
}

class CourseTable(var resource: Entity[_], var kind: String) {

  var lessons: collection.mutable.Buffer[Lesson] = _

  var activities: collection.mutable.Buffer[CourseActivity] = Collections.newBuffer[CourseActivity]

  var credits: java.lang.Float = null

  def getCredits(): java.lang.Float = {
    if (null == credits) {
      if (null == lessons) {
        null
      } else {
        var credits = 0F
        for (lesson <- lessons) {
          credits += lesson.course.credits
        }
        new java.lang.Float(credits)
      }
    } else {
      credits
    }
  }

  def extractTaskFromActivity() {
    if (null == activities) {
      return
    }
    val lessonSet = Collections.newBuffer[Lesson]
    for (activity <- activities) {
      val taskInActivity = activity.lesson
      if (!lessonSet.contains(taskInActivity)) {
        lessonSet += taskInActivity
      }
    }
    lessons = Collections.newBuffer[Lesson]
    lessons ++= lessonSet
  }
}
