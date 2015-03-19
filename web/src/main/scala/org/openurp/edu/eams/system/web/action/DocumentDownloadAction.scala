package org.openurp.edu.eams.system.web.action

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.eams.system.doc.model.Document
import org.openurp.edu.eams.system.doc.model.ManagerDocument
import org.openurp.edu.eams.system.doc.model.StudentDocument
import org.openurp.edu.eams.system.doc.model.TeacherDocument
import org.openurp.edu.eams.system.doc.service.DocPath
import org.openurp.edu.eams.system.security.EamsUserCategory
import org.openurp.edu.eams.web.action.common.FileAction



class DocumentDownloadAction extends FileAction {

  override def index(): String = {
    val curProfileId = getInt("curProfileId")
    val classCondition = new StringBuffer("")
    var query: OqlBuilder[_ <: Document] = null
    val userCategoryId = getUserCategoryId
    if (userCategoryId == EamsUserCategory.MANAGER_USER) {
      classCondition.append("or document.class = ManagerDocument ")
      query = OqlBuilder.from(classOf[ManagerDocument], "document")
    } else if (userCategoryId == EamsUserCategory.STD_USER) {
      val std = getLoginStudent
      classCondition.append("or document.class = StudentDocument ")
      query = OqlBuilder.from(classOf[StudentDocument], "document")
      query.join("document.stdTypes", "stdType")
      query.join("document.departs", "department")
      query.where("stdType in (:stdType)", std.getType)
      query.where("department in (:department)", std.department)
    } else if (userCategoryId == EamsUserCategory.TEACHER_USER) {
      classCondition.append("or document.class = TeacherDocument ")
      query = OqlBuilder.from(classOf[TeacherDocument], "document")
    } else {
      throw new RuntimeException("unspported category")
    }
    classCondition.delete(0, 2)
    query.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    put("documents", entityDao.search(query))
    put("curProfileId", curProfileId)
    forward()
  }

  def download(): String = {
    val curProfileId = getInt("curProfileId")
    val documentId = getLong("document.id")
    var document: Document = null
    if (curProfileId == EamsUserCategory.STD_USER) {
      if (null != documentId) {
        document = entityDao.get(classOf[StudentDocument], Array(documentId))
          .get(0)
      }
    } else if (curProfileId == EamsUserCategory.TEACHER_USER) {
      if (null != documentId) {
        document = entityDao.get(classOf[TeacherDocument], Array(documentId))
          .get(0)
      }
    } else {
      if (null != documentId) {
        document = entityDao.get(classOf[ManagerDocument], Array(documentId))
          .get(0)
      }
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
