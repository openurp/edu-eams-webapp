package org.openurp.edu.eams.teach.lesson.task.service.genstrategy

import java.util.Map
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenObserver

import scala.collection.JavaConversions._

trait LessonGenStrategy {

  def gen(source: String, context: Map[String, Any], progressBar: TaskGenObserver): Unit

  def preview(source: String, context: Map[String, Any]): AnyRef
}
