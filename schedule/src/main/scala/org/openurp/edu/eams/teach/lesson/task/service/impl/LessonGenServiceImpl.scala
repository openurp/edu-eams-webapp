package org.openurp.edu.eams.teach.lesson.task.service.impl



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.edu.eams.teach.lesson.task.service.LessonGenService
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenObserver
import org.openurp.edu.eams.teach.lesson.task.service.genstrategy.LessonGenStrategy



class LessonGenServiceImpl extends BaseServiceImpl with LessonGenService {

  private var strategies: List[LessonGenStrategy] = CollectUtils.newArrayList()

  def gen(source: String, context: Map[String, Any], progressBar: TaskGenObserver) {
    for (strategy <- strategies) {
      strategy.gen(source, context, progressBar)
    }
  }

  def preview(source: String, context: Map[String, Any]): AnyRef = {
    for (strategy <- strategies) {
      val result = strategy.preview(source, context)
      if (result != null) {
        return result
      }
    }
    null
  }

  def setStrategies(strategies: List[LessonGenStrategy]) {
    this.strategies = strategies
  }
}
