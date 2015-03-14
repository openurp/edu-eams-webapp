package org.openurp.edu.eams.teach.lesson.task.service

import java.util.Collection
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.model.RequirePrefer

import scala.collection.JavaConversions._

trait RequirePreferService {

  def getPreference(preferenceId: java.lang.Long): RequirePrefer

  def getPreference(teacher: Teacher, course: Course): RequirePrefer

  def savePreference(preference: RequirePrefer): Unit

  def savePreferenceForTask(task: Lesson): Unit

  def updatePreference(preference: RequirePrefer): Unit

  def setPreferenceFor(taskIdSeq: String): Unit

  def setPreferenceFor(tasks: Collection[_]): Unit
}
