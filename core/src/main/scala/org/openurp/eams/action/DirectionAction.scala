package org.openurp.eams.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.edu.base.{Direction, Major}

class DirectionAction extends RestfulAction[Direction] {
  override def editSetting(entity: Direction) = {

    val majors = findItems(classOf[Major])
    put("majors", majors)

    super.editSetting(entity)
  }

  private def findItems[T <: Entity[_]](clazz: Class[T]): Seq[T] = {
    val query = OqlBuilder.from(clazz)
    query.orderBy("id")
    val items = entityDao.search(query)
    items
  }

}




