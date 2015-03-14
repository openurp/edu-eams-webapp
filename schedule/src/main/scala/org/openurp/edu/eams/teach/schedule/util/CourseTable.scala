package org.openurp.edu.eams.teach.schedule.util

import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.Entity
import org.openurp.base.Room
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.base.Program
import org.openurp.edu.eams.teach.schedule.web.action.CourseTableAction
import CourseTable._

import scala.collection.JavaConversions._

object CourseTable {

  val CLASS = "class"

  val STD = "std"

  val TEACHER = "teacher"

  val ROOM = "room"

  val PROGRAM = "program"

  val resourceClass = CollectUtils.newHashMap()

  resourceClass.put(CLASS, classOf[Adminclass])

  resourceClass.put(STD, classOf[Student])

  resourceClass.put(TEACHER, classOf[Teacher])

  resourceClass.put(ROOM, classOf[Classroom])

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

  var lessons: List[Lesson] = _

  var activities: List[CourseActivity] = CollectUtils.newArrayList()

  var credits: java.lang.Float = null

  def getCredits(): java.lang.Float = {
    if (null == credits) {
      if (null == lessons) {
        null
      } else {
        var credits = 0
        for (lesson <- lessons) {
          credits += lesson.getCourse.getCredits
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
    val lessonSet = CollectUtils.newHashSet()
    for (activity <- activities) {
      val taskInActivity = activity.getLesson
      if (!lessonSet.contains(taskInActivity)) {
        lessonSet.add(taskInActivity)
      }
    }
    lessons = CollectUtils.newArrayList(lessonSet)
  }

  def getKind(): String = kind

  def setKind(kind: String) {
    this.kind = kind
  }

  def getResource(): Entity[_] = resource

  def setResource(resource: Entity[Long]) {
    this.resource = resource
  }

  def getLessons(): List[Lesson] = lessons

  def setLessons(lessons: List[Lesson]) {
    this.lessons = lessons
  }

  def getActivities(): List[CourseActivity] = activities

  def setActivities(activities: List[CourseActivity]) {
    this.activities = activities
  }

  def setCredits(credits: java.lang.Float) {
    this.credits = credits
  }
}
