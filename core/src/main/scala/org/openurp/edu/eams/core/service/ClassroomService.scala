package org.openurp.edu.eams.core.service


import org.openurp.base.Building
import org.openurp.base.Room



trait RoomService {

  def getRoom(id: java.lang.Integer): Room

  def getBuildings(campusId: java.lang.Integer): List[Building]

  def saveOrUpdate(classroom: Room): Unit

  def removeRoom(id: java.lang.Integer): Unit
}
