package org.openurp.edu.eams.teach.lesson.task.experiment






class LessonGenProgressBar extends AbstractAjaxProgressBar {

  
  var planCount: Int = _

  def start() {
    notify(Status.GOOD, "现在开始生成任务", false, new Pair[String, String]("total", planCount + ""))
  }

  override def finish() {
    notify(Status.GOOD, "任务生成结束", false)
    super.finish()
  }
}
