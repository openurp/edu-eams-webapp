package org.openurp.edu.eams.system.web.action

import java.io.File
import java.sql.Date

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.ems.config.model.PropertyConfigItemBean
import org.beangle.security.codec.EncryptUtil
import org.openurp.base.Department
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.system.doc.model.Document
import org.openurp.edu.eams.system.doc.model.ManagerDocument
import org.openurp.edu.eams.system.doc.model.StudentDocument
import org.openurp.edu.eams.system.doc.model.TeacherDocument
import org.openurp.edu.eams.system.doc.service.DocPath
import org.openurp.edu.eams.web.action.common.FileAction



class DocumentAction extends FileAction {

  override def search(): String = {
    var kind = get("kind")
    if (null == kind) {
      kind = "manager"
    }
    var query: OqlBuilder[_ <: Document] = null
    if ("std" == kind) {
      query = OqlBuilder.from(classOf[StudentDocument], "document")
    } else if ("teacher" == kind) {
      query = OqlBuilder.from(classOf[TeacherDocument], "document")
    } else if ("manager" == kind) {
      query = OqlBuilder.from(classOf[ManagerDocument], "document")
    }
    query.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    put("kind", kind)
    put("documents", entityDao.search(query))
    forward()
  }

  override def info(): String = {
    val id = getLongId("document")
    var kind = get("kind")
    if (null == kind) {
      kind = "manager"
    }
    if (kind == "std") {
      put("document", entityDao.get(classOf[StudentDocument], id))
    } else if (kind == "teacher") {
      put("document", entityDao.get(classOf[TeacherDocument], id))
    } else if (kind == "manager") {
      put("document", entityDao.get(classOf[ManagerDocument], id))
    }
    put("kind", kind)
    forward()
  }

  protected def getPath(typeName: String): String = {
    val configItems = entityDao.get(classOf[PropertyConfigItemBean], "name", typeName)
    if (configItems.size > 0) configItems.get(0).getValue else ""
  }

  override def remove(): String = {
    val kind = get("kind")
    val documentIds = getLongIds("document")
    val contextPath = getPath(DocPath.DOC_PATH)
    try {
      if (kind == "std") {
        val documents = entityDao.get(classOf[StudentDocument], documentIds)
        for (document <- documents) {
          entityDao.remove(document)
          val a = new File(contextPath + "\\" + document.getPath)
          if (a.delete()) {
            logger.info("user [" + getUser + "] delete file[" + a.getAbsolutePath + 
              "]")
          }
        }
      } else if (kind == "teacher") {
        val documents = entityDao.get(classOf[TeacherDocument], documentIds)
        for (document <- documents) {
          entityDao.remove(document)
          val a = new File(contextPath + "\\" + document.getPath)
          if (a.delete()) {
            logger.info("user [" + getUser + "] delete file[" + a.getAbsolutePath + 
              "]")
          }
        }
      } else if (kind == "manager") {
        val documents = entityDao.get(classOf[ManagerDocument], documentIds)
        for (document <- documents) {
          entityDao.remove(document)
          val a = new File(contextPath + "\\" + document.getPath)
          if (a.delete()) {
            logger.info("user [" + getUser + "] delete file[" + a.getAbsolutePath + 
              "]")
          }
        }
      }
      redirect("search", "info.delete.success", "&kind=" + get("kind"))
    } catch {
      case e: Exception => {
        logger.info("remove failure", e)
        redirect("search", "info.delete.failure", "&kind=" + get("kind"))
      }
    }
  }

  def upload(): String = {
    val f = upload(getPath(DocPath.DOC_PATH))
    if (f != null) return f
    redirect("search", "info.upload.success", "&kind=" + get("kind"))
  }

  protected override def getFileName(uploadAbsolutePath: String): String = {
    val commaIndex = uploadAbsolutePath.lastIndexOf(".")
    if (-1 != commaIndex) {
      EncryptUtil.encode(uploadAbsolutePath + System.currentTimeMillis()) + 
        uploadAbsolutePath.substring(commaIndex, uploadAbsolutePath.length)
    } else {
      EncryptUtil.encode(uploadAbsolutePath + System.currentTimeMillis())
    }
  }

  protected override def afterUpload(file: File, updloadDocPath: String) {
    val kind = get("kind")
    var document: Document = null
    if ("std" == kind) {
      val stdTypesId = Strings.splitToInt(get("stdTypeIds"))
      val departmentIds = Strings.splitToInt(get("departmentIds"))
      val stdDocument = new StudentDocument()
      if (null != stdTypesId) {
        val stdTypes = entityDao.get(classOf[StdType], stdTypesId)
        stdDocument.stdTypes.addAll(stdTypes)
      }
      if (null != departmentIds) {
        val deaprtments = entityDao.get(classOf[Department], departmentIds)
        stdDocument.getDeparts.addAll(deaprtments)
      }
      document = stdDocument
    } else if ("teacher" == kind) {
      document = new TeacherDocument()
    } else if ("manager" == kind) {
      document = new ManagerDocument()
    } else {
      throw new RuntimeException("unspported kind")
    }
    updloadDocPath = updloadDocPath.substring(updloadDocPath.lastIndexOf("\\") + 1)
    updloadDocPath = updloadDocPath.substring(updloadDocPath.lastIndexOf("/") + 1)
    document.setName(updloadDocPath)
    document.setPath(file.getName)
    document.setUploadBy(getUsername)
    document.setUploadOn(new Date(System.currentTimeMillis()))
    entityDao.saveOrUpdate(document)
    logger.info("user [" + document.getUploadBy + "] upload file[" + file.getAbsolutePath + 
      "]")
  }

  def uploadSetting(): String = {
    addBaseCode("stdTypes", classOf[StdType])
    addBaseInfo("departments", classOf[Department])
    put("kind", get("kind"))
    forward()
  }

  def download(): String = {
    val kindString = get("kind")
    val documentId = getLong("document.id")
    var document: Document = null
    if (kindString == "std") {
      if (null != documentId) document = entityDao.get(classOf[StudentDocument], Array(documentId))
        .get(0)
    } else if (kindString == "teacher") {
      if (null != documentId) document = entityDao.get(classOf[TeacherDocument], Array(documentId))
        .get(0)
    } else if (kindString == "manager") {
      if (null != documentId) document = entityDao.get(classOf[ManagerDocument], Array(documentId))
        .get(0)
    }
    if (null == document) {
      put("documentId", documentId)
      return forward("noFile")
    }
    var docRepoPath = getFileRealPath(DocPath.DOC)
    if (!docRepoPath.endsWith("/")) {
      docRepoPath += "/"
    }
    download(docRepoPath + document.getPath, document.getName)
    null
  }
}
