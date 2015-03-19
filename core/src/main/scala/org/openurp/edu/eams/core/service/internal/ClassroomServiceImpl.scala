package org.openurp.edu.eams.core.service.internal

import java.sql.Date

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.base.Building
import org.openurp.base.Room
import org.openurp.edu.eams.core.service.RoomService



class RoomServiceImpl extends BaseServiceImpl with RoomService {

  def getRoom(id: java.lang.Integer): Room = entityDao.get(classOf[Room], id)

  def getBuildings(campusId: java.lang.Integer): List[Building] = {
    entityDao.get(classOf[Building], "campus.id", campusId)
  }

  def removeRoom(id: java.lang.Integer) {
    if (null == id) return
    entityDao.remove(entityDao.get(classOf[Room], id))
  }

  def saveOrUpdate(classroom: Room) {
    if (!classroom.isPersisted) {
      classroom.setCreatedAt(new Date(System.currentTimeMillis()))
    }
    classroom.setUpdatedAt(new Date(System.currentTimeMillis()))
    this.entityDao.saveOrUpdate(classroom)
  }
}
