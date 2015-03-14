package org.openurp.edu.eams.classroom.service

import java.util.Collection
import java.util.List
import org.openurp.base.Classroom
import org.openurp.edu.eams.classroom.TimeUnit

import scala.collection.JavaConversions._

trait ClassroomResourceService {

  def getFreeRooms(rooms: Collection[Classroom], unit: TimeUnit): List[Classroom]
}
