package org.openurp.edu.eams.web.util

import java.io.PrintWriter
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

class OutputProcessObserver extends OutputObserver() {

  protected var writer: PrintWriter = _

  protected var textResource: TextResource = _

  protected var logger: Logger = LoggerFactory.getLogger(classOf[OutputProcessObserver])

  def this(writer: PrintWriter, textResource: TextResource) {
    this()
    this.writer = writer
    this.textResource = textResource
  }

  def setSummary(msg: String) {
    try {
      writer.write("<script>setSummary('" + msg + "')</script>\n")
      writer.flush()
    } catch {
      case e: Exception => logger.warn("exception", e)
    }
  }

  def setOverallCount(count: Int) {
    try {
      writer.write("<script>count=" + count + "</script>" + "\n")
      writer.flush()
    } catch {
      case e: Exception => logger.warn("exception", e)
    }
  }

  def notifyStart(summary: String, count: Int, msgs: Array[String]) {
    try {
      setSummary(summary)
      setOverallCount(count)
      if (null != msgs) {
        for (i <- 0 until msgs.length) {
          writer.write("<script>addProcessMsg('" + msgs(i) + "');</script>\n")
        }
        writer.flush()
      }
    } catch {
      case e: Exception => logger.warn("exception", e)
    }
  }

  def outputNotify(level: Int, msgObj: OutputMessage, increaceProcess: Boolean) {
    try {
      if (increaceProcess) writer.print("<script>addProcessMsg(" + level + ",\"" + message(msgObj) + 
        "\",1);</script>\n") else writer.print("<script>addProcessMsg(" + level + ",\"" + message(msgObj) + 
        "\",0);</script>\n")
      writer.flush()
    } catch {
      case e: Exception => logger.warn("exception", e)
    }
  }

  def outputNotify(level: Int, msgObj: OutputMessage) {
    outputNotify(level, msgObj, true)
  }

  def message(msgObj: OutputMessage): String = msgObj.getMessage(textResource)

  def getWriter(): PrintWriter = writer

  def setWriter(writer: PrintWriter) {
    this.writer = writer
  }

  def messageOf(key: String): String = {
    if (Strings.isNotEmpty(key)) {
      textResource.getText(key)
    } else {
      ""
    }
  }

  def messageOf(key: String, arg0: AnyRef): String = {
    if (Strings.isNotEmpty(key)) {
      textResource.getText(key, key, arg0)
    } else {
      ""
    }
  }

  def notifyFinish() {
    writer.println("finish")
  }

  def notifyStart() {
    writer.println("start")
  }

  def getTextResource(): TextResource = textResource

  def setTextResource(textResource: TextResource) {
    this.textResource = textResource
  }
}
