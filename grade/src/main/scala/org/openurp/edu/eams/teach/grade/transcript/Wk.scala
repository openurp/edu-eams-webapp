package org.openurp.edu.eams.teach.grade.transcript

import java.io.BufferedReader
import java.io.InputStreamReader
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class Wk(@BeanProperty var exeName: String, @BeanProperty var pageUrl: String, @BeanProperty var imageUrl: String)
    {

  @BeanProperty
  var msg: String = ""

  def exec(): Int = windowExe()

  private def windowExe(): Int = {
    if (exeName.isEmpty) {
      msg = "exe不能为空"
      return -1
    }
    if (pageUrl.isEmpty) {
      msg = "页面路径不能为空"
      return -1
    }
    if (imageUrl.isEmpty) {
      msg = "图片名称不能为空"
      return -1
    }
    try {
      val pb = new ProcessBuilder()
      pb.redirectErrorStream(true)
      pb.command(Array(exeName, pageUrl, imageUrl))
      val p = pb.start()
      waitFor(p)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        msg = e.getMessage
        -2
      }
    }
  }

  private def linuxExe(): Int = 0

  private def waitFor(p: Process): Int = {
    val br = new BufferedReader(new InputStreamReader(p.getInputStream))
    try {
      while (br.readLine() != null) {
      }
      val exitValue = p.waitFor()
      msg = if (exitValue == 0) "success" else "failure"
      exitValue
    } catch {
      case e: Exception => {
        e.printStackTrace()
        msg = e.getMessage
        -1
      }
    }
  }
}
