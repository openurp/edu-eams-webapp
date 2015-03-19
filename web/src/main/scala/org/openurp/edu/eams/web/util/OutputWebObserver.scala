package org.openurp.edu.eams.web.util

import java.io.InputStreamReader
import java.io.LineNumberReader
import java.io.PrintWriter
import org.beangle.commons.text.i18n.TextResource
import org.springframework.core.io.ClassPathResource



class OutputWebObserver extends OutputProcessObserver {

  protected var path: String = _

  def this(writer: PrintWriter, textResource: TextResource, path: String) {
    super(writer, textResource)
    this.path = path
    outputTemplate()
  }

  def outputTemplate() {
    try {
      val cpr = new ClassPathResource("/template/" + path)
      val reader = new LineNumberReader(new InputStreamReader(cpr.getInputStream, "UTF-8"))
      var lineContent: String = null
      do {
        lineContent = reader.readLine()
        if (null != lineContent) {
          writer.write(lineContent + "\r\n")
        }
      } while (null != lineContent);
      writer.flush()
    } catch {
      case e: Exception => logger.warn("exception", e)
    }
  }

  def this(writer: PrintWriter, textResource: TextResource) {
    this()
    this.writer = writer
    this.textResource = textResource
  }

  def outputNotify(level: Int, msgObj: OutputMessage) {
    try level match {
      case 1 => 
        writer.print(message(msgObj))
        writer.flush()

      case 2 => 
        writer.print("<font color=blue>")
        writer.print(message(msgObj))
        writer.print("</font>")
        writer.flush()

      case 3 => 
        writer.print("<font color=red>")
        writer.print(message(msgObj))
        writer.print("</font>")
        writer.flush()

      case 4 => 
        writer.print("<font color='green'>")
        writer.print(message(msgObj))
        writer.print("</font>")
        writer.flush()

    } catch {
      case e: Exception => logger.warn("exception", e)
    }
  }

  def outputNotify(msgObj: OutputMessage) {
    outputNotify(OutputObserver.good, msgObj)
  }

  def getPath(): String = path

  def setPath(path: String) {
    this.path = path
  }
}
