package org.openurp.eams.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.model.code.CodeMeta
import org.openurp.edu.base.Project
import org.openurp.edu.base.model.ProjectCodeBean

class ProjectCodeAction extends RestfulAction[ProjectCodeBean] {
  override def editSetting(entity: ProjectCodeBean) = {
    val projects = findItems(classOf[Project])
    put("projects", projects)

    val metas = findItems(classOf[CodeMeta])
    put("metas", metas)

    super.editSetting(entity)
  }

  private def findItems[T <: Entity[_]](clazz: Class[T]): Seq[T] = {
    val query = OqlBuilder.from(clazz)
    query.orderBy("name")
    val items = entityDao.search(query)
    items
  }

}