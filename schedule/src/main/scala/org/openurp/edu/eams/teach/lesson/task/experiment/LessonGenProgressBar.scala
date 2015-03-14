package org.openurp.edu.eams.teach.lesson.task.experiment

import org.beangle.commons.lang.tuple.Pair
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class LessonGenProgressBar extends AbstractAjaxProgressBar {

  @BeanProperty
  var planCount: Int = _

  def start() {
    notify(Status.GOOD, "现在开始生成任务", false, new Pair[String, String]("total", planCount + ""))
  }

  override def finish() {
    notify(Status.GOOD, "任务生成结束", false)
    super.finish()
  }
}
