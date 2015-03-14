package org.openurp.edu.eams.teach.lesson.task.service


import scala.collection.JavaConversions._

trait LessonCollegeSwitchService {

  def allow(semesterId: java.lang.Integer, projectId: java.lang.Integer): Unit

  def disallow(semesterId: java.lang.Integer, projectId: java.lang.Integer): Unit

  def status(semesterId: java.lang.Integer, projectId: java.lang.Integer): Boolean
}
