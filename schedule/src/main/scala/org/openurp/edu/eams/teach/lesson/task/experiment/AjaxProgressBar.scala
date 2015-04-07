package org.openurp.edu.eams.teach.lesson.task.experiment

import java.util.concurrent.BlockingQueue
import AjaxProgressBar._
import ch.qos.logback.core.status.Status



object AjaxProgressBar {

  val OVER_MSG = "{status : 'GOOD', message : '!!!GAME_OVER!!!', increase : false}"

  val DESTORY_DELAY = 1000 * 60 * 30

  object Status extends Enumeration {

    val GOOD = new Status()

    val WARNING = new Status()

    val ERROR = new Status()

    class Status extends Val

    implicit def convertValue(v: Value): Status = v.asInstanceOf[Status]
  }
}

trait AjaxProgressBar {

  def id(): Long

  def getMessagePool(): BlockingQueue[String]

  def start(): Unit

  def notify(kind: Status, message: String, increase: Boolean): Unit

  def notify(kind: Status, 
      message: String, 
      increase: Boolean, 
      extras: Pair[String, String]*): Unit

  def finish(): Unit
}
