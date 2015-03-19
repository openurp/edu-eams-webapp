package org.openurp.edu.eams.web.util

import java.io.IOException
import java.io.PrintWriter
import OutputObserver._



object OutputObserver {

  var good: Int = 1

  var warnning: Int = 2

  var error: Int = 3
}

trait OutputObserver {

  def outputNotify(level: Int, arg2: OutputMessage): Unit

  def notifyStart(): Unit

  def notifyFinish(): Unit

  def message(msgObj: OutputMessage): String

  def messageOf(key: String): String

  def messageOf(key: String, arg0: AnyRef): String

  def getWriter(): PrintWriter

  def setWriter(writer: PrintWriter): Unit
}
