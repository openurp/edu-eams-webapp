package org.openurp.edu.eams.web.action.api

import java.util.List
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.teach.lesson.model.NormalClassBean
import org.openurp.edu.eams.web.action.common.ProjectSupportAction

import scala.collection.JavaConversions._

class NormalclassAction extends ProjectSupportAction {

  def json(): String = {
    val query = OqlBuilder.from(classOf[NormalClassBean], "normalclass")
    query.where("normalclass.project.id = :projectId", getProject.getId)
    query.orderBy("normalclass.code")
    val list = entityDao.search(query)
    put("normalclasses", list)
    forward()
  }
}
