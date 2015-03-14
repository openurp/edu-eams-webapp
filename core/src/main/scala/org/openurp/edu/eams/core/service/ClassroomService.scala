package org.openurp.edu.eams.core.service

import java.util.List
import org.openurp.base.Building
import org.openurp.base.Room

import scala.collection.JavaConversions._

trait ClassroomService {

  def getClassroom(id: java.lang.Integer): Room

  def getBuildings(campusId: java.lang.Integer): List[Building]

  def saveOrUpdate(classroom: Room): Unit

  def removeClassroom(id: java.lang.Integer): Unit
}
