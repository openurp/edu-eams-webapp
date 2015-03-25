package org.openurp.edu.eams.web.action.common

import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.sql.Date
import org.apache.commons.io.FileUtils
import org.apache.struts2.ServletActionContext
import org.beangle.commons.web.io.DefaultStreamDownloader
import org.beangle.commons.web.io.StreamDownloader
import org.beangle.security.codec.EncryptUtil
import org.openurp.edu.eams.system.doc.service.DocPath



class FileAction extends RestrictionSupportAction {

  var streamDownloader: StreamDownloader = _

  protected def download(fileAbsolutePath: String) {
    download(fileAbsolutePath, getDownloadFileName(fileAbsolutePath))
  }

  protected def download(fileAbsolutePath: String, displayFileName: String) {
    val file = new File(fileAbsolutePath)
    if (!file.exists()) {
      ServletActionContext.getResponse.getWriter.write("without file path:[" + fileAbsolutePath + "]")
      return
    }
    streamDownloader.download(ServletActionContext.getRequest, ServletActionContext.getResponse, file, 
      displayFileName)
  }

  protected def upload(storeAbsolutePath: String): String = {
    if (!DocPath.isPathExists(storeAbsolutePath)) {
      put("DocPath", storeAbsolutePath)
      return forward("../systemConfig/filePathError")
    }
    if (!storeAbsolutePath.endsWith(File.separator)) {
      storeAbsolutePath += File.separator
    }
    processFiles("file1", storeAbsolutePath)
    processFiles("file2", storeAbsolutePath)
    processFiles("file3", storeAbsolutePath)
    null
  }

  protected def processFiles(paramName: String, storeAbsolutePath: String) {
    val fileNames = getAll(paramName + "FileName").asInstanceOf[Array[String]]
    if (null == fileNames) {
      return
    }
    val files = getAll(paramName).asInstanceOf[Array[File]]
    for (i <- 0 until files.length) {
      val fileName = fileNames(i)
      val newFile = new File(storeAbsolutePath + getFileName(fileName))
      FileUtils.copyFile(files(i), newFile)
      afterUpload(newFile, fileName)
    }
  }

  protected def afterUpload(file: File, updloadDocPath: String) {
    logger.info(" user upload file from [" + updloadDocPath + "] and store at[" + 
      file.getAbsolutePath + 
      "] on" + 
      new Date(System.currentTimeMillis()))
  }

  protected def getFileName(uploadAbsolutePath: String): String = {
    val commaIndex = uploadAbsolutePath.lastIndexOf(".")
    if (-1 != commaIndex) {
      EncryptUtil.encode(uploadAbsolutePath) + 
        uploadAbsolutePath.substring(commaIndex, uploadAbsolutePath.length)
    } else {
      EncryptUtil.encode(uploadAbsolutePath)
    }
  }

  def getDownloadFileName(fileAbsolutePath: String): String = {
    val attch_name = DefaultStreamDownloader.getAttachName(fileAbsolutePath, null)
    URLEncoder.encode(attch_name, "utf-8")
  }

  protected def getFileRealPath(kind: String): String = {
    DocPath.getRealPath(getConfig, kind, ServletActionContext.getServletContext.getRealPath(DocPath.fileDirectory))
  }
}
