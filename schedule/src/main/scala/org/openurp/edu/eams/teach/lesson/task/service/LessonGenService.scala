package org.openurp.edu.eams.teach.lesson.task.service





trait LessonGenService {

  def gen(source: String, context: Map[String, Any], progressBar: TaskGenObserver): Unit

  def preview(source: String, context: Map[String, Any]): AnyRef
}
