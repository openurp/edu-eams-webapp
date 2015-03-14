package org.openurp.edu.eams.teach.grade.transcript

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class FilesToZip(zipFileName: String) {

  @BeanProperty
  var zipOut: ZipOutputStream = _

  val file = new File(zipFileName)

  try {
    zipOut = new ZipOutputStream(new FileOutputStream(file))
  } catch {
    case e: FileNotFoundException => e.printStackTrace()
  }

  zipOut.setEncoding("gbk")

  def close(): String = {
    try {
      zipOut.close()
    } catch {
      case e: IOException => {
        e.printStackTrace()
        return e.getMessage
      }
    }
    ""
  }

  def zipFile(fileName: String): String = {
    val file = new File(fileName)
    val zipEntry = new ZipEntry(fileName)
    try {
      zipOut.putNextEntry(zipEntry)
      val in = new FileInputStream(file)
      var b = -1
      while ((b = in.read()) != -1) {
        zipOut.write(b)
      }
      in.close()
    } catch {
      case e: Exception => {
        e.printStackTrace()
        return e.getMessage
      }
    }
    file.delete()
    ""
  }
}
