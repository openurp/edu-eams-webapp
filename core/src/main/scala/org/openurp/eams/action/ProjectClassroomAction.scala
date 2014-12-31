package org.openurp.eams.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.{ Department, Room }
import org.openurp.edu.base.{ Project, ProjectClassroom }
import org.openurp.edu.base.model.ProjectClassroomBean

class ProjectClassroomAction extends RestfulAction[ProjectClassroom] {
  override def editSetting(entity: ProjectClassroom) = {
    val projects = findItems(classOf[Project])
    put("projects", projects)

    val rooms = findItems(classOf[Room])
    put("rooms", rooms)

    val departs = findItems(classOf[Department])
    put("departs", departs)

    super.editSetting(entity)
  }

  private def findItems[T <: Entity[_]](clazz: Class[T]): Seq[T] = {
    val query = OqlBuilder.from(clazz)
    query.orderBy("name")
    val items = entityDao.search(query)
    items
  }

  protected override def saveAndRedirect(entity: ProjectClassroom): View = {

    val projectClassroom = entity.asInstanceOf[ProjectClassroomBean]

    projectClassroom.departs.clear()
    val departsIds = getAll("departsId2nd", classOf[Integer])
    projectClassroom.departs ++= entityDao.find(classOf[Department], departsIds)

    super.saveAndRedirect(entity)
  }

}

