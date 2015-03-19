package org.openurp.edu.eams.teach.lesson.task.service.genstrategy


import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenObserver



abstract class AbstractLessonGenStrategy extends BaseServiceImpl with LessonGenStrategy {

  protected def iDo(source: String): Boolean

  protected var teachClassNameStrategy: TeachClassNameStrategy = _

  def setTeachClassNameStrategy(teachClassNameStrategy: TeachClassNameStrategy) {
    this.teachClassNameStrategy = teachClassNameStrategy
  }

  def gen(source: String, context: Map[String, Any], progressBar: TaskGenObserver) {
    if (!iDo(source)) {
      return
    }
    gen(context, progressBar)
  }

  def preview(source: String, context: Map[String, Any]): AnyRef = {
    if (!iDo(source)) {
      return null
    }
    preview(context)
  }

  protected def gen(context: Map[String, Any], progressBar: TaskGenObserver): Unit

  protected def preview(context: Map[String, Any]): AnyRef
}
