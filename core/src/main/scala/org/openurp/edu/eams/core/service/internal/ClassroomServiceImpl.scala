package org.openurp.edu.eams.core.service.internal

import java.sql.Date
import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.base.Building
import org.openurp.base.Classroom
import org.openurp.edu.eams.core.service.ClassroomService

import scala.collection.JavaConversions._

class ClassroomServiceImpl extends BaseServiceImpl with ClassroomService {

  def getClassroom(id: java.lang.Integer): Classroom = entityDao.get(classOf[Classroom], id)

  def getBuildings(campusId: java.lang.Integer): List[Building] = {
    entityDao.get(classOf[Building], "campus.id", campusId)
  }

  def removeClassroom(id: java.lang.Integer) {
    if (null == id) return
    entityDao.remove(entityDao.get(classOf[Classroom], id))
  }

  def saveOrUpdate(classroom: Classroom) {
    if (!classroom.isPersisted) {
      classroom.setCreatedAt(new Date(System.currentTimeMillis()))
    }
    classroom.setUpdatedAt(new Date(System.currentTimeMillis()))
    this.entityDao.saveOrUpdate(classroom)
  }
}
