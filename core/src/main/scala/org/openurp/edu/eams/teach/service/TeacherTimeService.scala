package org.openurp.edu.eams.teach.service

import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.base.Teacher

import scala.collection.JavaConversions._

trait TeacherTimeService {

  def isOccupied(time: TimeUnit, teachers: Teacher*): Boolean
}
