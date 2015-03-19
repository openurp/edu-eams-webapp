package org.openurp.edu.eams.web.action.api


import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.eams.teach.lesson.model.NormalClassBean
import org.openurp.edu.eams.web.action.common.ProjectSupportAction



class NormalclassAction extends ProjectSupportAction {

  def json(): String = {
    val query = OqlBuilder.from(classOf[NormalClassBean], "normalclass")
    query.where("normalclass.project.id = :projectId", getProject.id)
    query.orderBy("normalclass.code")
    val list = entityDao.search(query)
    put("normalclasses", list)
    forward()
  }
}
