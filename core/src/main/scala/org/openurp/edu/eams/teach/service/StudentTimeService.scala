package org.openurp.edu.eams.teach.service

import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.base.Student

import scala.collection.JavaConversions._

trait StudentTimeService {

  def isOccupied(time: TimeUnit, stds: Student*): Boolean
}
