package org.openurp.edu.eams.teach.lesson.task.experiment

import java.util.concurrent.ConcurrentHashMap



object AjaxProgressBarFactory {

  var barPool: ConcurrentHashMap[Long, AjaxProgressBar] = new ConcurrentHashMap[Long, AjaxProgressBar]()

  def getInstance(clazz: Class[_ <: AjaxProgressBar]): AjaxProgressBar = {
    val bar = clazz.newInstance()
    barPool.putIfAbsent(bar.id, bar)
    bar
  }

  def take(barId: java.lang.Long): String = {
    val bar = barPool.get(barId)
    if (bar == null) {
      return AjaxProgressBar.OVER_MSG
    }
    bar.getMessagePool.take()
  }
}
