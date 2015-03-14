package org.openurp.edu.eams.teach.lesson.task.service

import java.util.Map

import scala.collection.JavaConversions._

trait LessonGenService {

  def gen(source: String, context: Map[String, Any], progressBar: TaskGenObserver): Unit

  def preview(source: String, context: Map[String, Any]): AnyRef
}
