package org.openurp.edu.eams.teach.lesson.task.service


import org.openurp.edu.base.Teacher
import org.openurp.edu.base.Course
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.model.RequirePrefer



trait RequirePreferService {

  def getPreference(preferenceId: java.lang.Long): RequirePrefer

  def getPreference(teacher: Teacher, course: Course): RequirePrefer

  def savePreference(preference: RequirePrefer): Unit

  def savePreferenceForTask(task: Lesson): Unit

  def updatePreference(preference: RequirePrefer): Unit

  def setPreferenceFor(taskIdSeq: String): Unit

  def setPreferenceFor(tasks: Iterable[_]): Unit
}
