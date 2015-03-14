package org.openurp.edu.eams.system.web.action

import javax.servlet.http.HttpServletResponse
import org.beangle.commons.entity.util.ValidEntityKeyPredicate
import org.openurp.edu.eams.system.doc.model.Document
import org.openurp.edu.eams.system.doc.service.DocPath
import org.openurp.edu.eams.web.action.common.FileAction

import scala.collection.JavaConversions._

class DataTemplateAction extends FileAction {

  def download() {
    val documentId = getLong("document.id")
    val response = getResponse
    if (!ValidEntityKeyPredicate.Instance.apply(documentId)) {
      response.getWriter.write("without template with id:" + documentId)
      return
    }
    val document = entityDao.get(classOf[Document], documentId).asInstanceOf[Document]
    if (null == document) {
      response.getWriter.write("without template with id:" + documentId)
      return
    }
    download(getFileRealPath(DocPath.TEMPLATE_UPLOAD) + document.getPath)
  }
}
