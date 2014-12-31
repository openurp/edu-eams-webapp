package org.openurp.eams.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.Department
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Major
import org.openurp.edu.base.model.AdminclassBean
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Teacher
import org.openurp.edu.base.code.StdType

class AdminclassAction extends RestfulAction[Adminclass] {
  override def editSetting(entity: Adminclass) = {
    val departments = findItems(classOf[Department])
    put("departments", departments)

    val majors = findItems(classOf[Major])
    put("majors", majors)

    val directions = findItems(classOf[Direction])
    put("directions", directions)

    val stdTypes = findItems(classOf[StdType])
    put("stdTypes", stdTypes)

    val instructors = findItems(classOf[Teacher])
    put("instructors", List.empty)

    val tutors = findItems(classOf[Teacher])
    put("tutors", List.empty)

    super.editSetting(entity)
  }

  private def findItems[T <: Entity[_]](clazz: Class[T]): Seq[T] = {
    val query = OqlBuilder.from(clazz)
    query.orderBy("name")
    val items = entityDao.search(query)
    items
  }
  protected override def saveAndRedirect(entity: Adminclass): View = {
    val adminclass = entity.asInstanceOf[AdminclassBean]

    adminclass.instructors.asInstanceOf[collection.mutable.Buffer[Teacher]].clear()
    val instructorIds = getAll("instructorsId2nd", classOf[Integer])
    adminclass.instructors ++= entityDao.find(classOf[Teacher], instructorIds)

    adminclass.tutors.asInstanceOf[collection.mutable.Buffer[Teacher]].clear()
    val tutorIds = getAll("tutorsId2nd", classOf[Integer])
    adminclass.tutors ++= entityDao.find(classOf[Teacher], tutorIds)

    super.saveAndRedirect(entity)
  }

}

