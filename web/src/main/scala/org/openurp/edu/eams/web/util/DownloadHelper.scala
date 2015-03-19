package org.openurp.edu.eams.web.util

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.RequestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory



object DownloadHelper {

  private var logger: Logger = LoggerFactory.getLogger(classOf[DownloadHelper])

  def download(request: HttpServletRequest, response: HttpServletResponse, file: File) {
    download(request, response, file, file.getName)
  }

  def download(request: HttpServletRequest, 
      response: HttpServletResponse, 
      url: URL, 
      display: String) {
    try {
      download(request, response, url.openStream(), url.getFile, display)
    } catch {
      case e: Exception => logger.warn("download file error=" + display, e)
    }
  }

  def download(request: HttpServletRequest, 
      response: HttpServletResponse, 
      file: File, 
      display: String) {
    try {
      download(request, response, new FileInputStream(file), file.getAbsolutePath, display)
    } catch {
      case e: Exception => logger.warn("download file error=" + display, e)
    }
  }

  def download(request: HttpServletRequest, 
      response: HttpServletResponse, 
      inStream: InputStream, 
      name: String, 
      display: String) {
    var attch_name = ""
    val b = Array.ofDim[Byte](1024)
    var len = 0
    try {
      val ext = Strings.substringAfterLast(name, ".")
      if (Strings.isBlank(display)) {
        attch_name = getAttachName(name)
      } else {
        attch_name = display
        if (!attch_name.endsWith("." + ext)) {
          attch_name += "." + ext
        }
      }
      response.reset()
      var contentType = response.getContentType
      if (null == contentType) {
        contentType = "application/x-msdownload"
        response.setContentType(contentType)
        logger.debug("set content type {} for {}", contentType, attch_name)
      }
      response.addHeader("Content-Disposition", "attachment; filename=\"" + RequestUtils.encodeAttachName(request, 
        attch_name) + 
        "\"")
      while ((len = inStream.read(b)) > 0) {
        response.getOutputStream.write(b, 0, len)
      }
      inStream.close()
    } catch {
      case e: Exception => logger.warn("download file error=" + attch_name, e)
    }
  }

  def getAttachName(file_name: String): String = {
    if (file_name == null) return ""
    file_name = file_name.trim()
    var iPos = 0
    iPos = file_name.lastIndexOf("\\")
    if (iPos > -1) {
      file_name = file_name.substring(iPos + 1)
    }
    iPos = file_name.lastIndexOf("/")
    if (iPos > -1) {
      file_name = file_name.substring(iPos + 1)
    }
    iPos = file_name.lastIndexOf(File.separator)
    if (iPos > -1) {
      file_name = file_name.substring(iPos + 1)
    }
    file_name
  }
}
