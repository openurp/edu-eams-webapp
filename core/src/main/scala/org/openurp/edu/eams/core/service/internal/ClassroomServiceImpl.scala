package org.openurp.edu.eams.core.service.internal

import java.sql.Date

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.base.Building
import org.openurp.base.Room
import org.openurp.edu.eams.core.service.RoomService

class RoomServiceImpl extends BaseServiceImpl with RoomService {

  def getRoom(id: java.lang.Integer): Room = entityDao.get(classOf[Room], id)

  def getBuildings(campusId: java.lang.Integer): Seq[Building] = {
    entityDao.findBy(classOf[Building], "campus.id", List(campusId))
  }

  def removeRoom(id: java.lang.Integer) {
    if (null == id) return
    entityDao.remove(entityDao.get(classOf[Room], id))
  }

  def saveOrUpdate(classroom: Room) {
    //    if (!classroom.isPersisted) {
    //      classroom.createdAt=new Date(System.currentTimeMillis())
    //    }
    //    classroom.updatedAt=new Date(System.currentTimeMillis())
    this.entityDao.saveOrUpdate(classroom)
  }
}
