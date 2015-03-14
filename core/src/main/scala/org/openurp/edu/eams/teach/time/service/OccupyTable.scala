package org.openurp.edu.eams.teach.time.service

import java.util.List
import OccupyTable._

import scala.collection.JavaConversions._

object OccupyTable {

  val TABLE_NAME = "occupyTable"
}

trait OccupyTable {

  def addOccupy(activites: List[_]): Unit

  def removeOccupy(activites: List[_]): Unit

  def isConflict(activites: List[_]): Boolean

  def getWeekState(week: Int, unit: Int): Number

  def getWeekStateStr(week: Int, unit: Int): String
}
