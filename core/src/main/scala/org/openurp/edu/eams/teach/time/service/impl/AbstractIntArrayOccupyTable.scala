package org.openurp.edu.eams.teach.time.service.impl

import java.io.Serializable
import org.openurp.edu.eams.teach.time.service.OccupyTable

import scala.collection.JavaConversions._

@SerialVersionUID(-2264610685600348371L)
abstract class AbstractIntArrayOccupyTable extends OccupyTable with Serializable with Cloneable {

  protected var occupy: Array[Array[Int]] = _

  def getWeekState(week: Int, unit: Int): Number = {
    new java.lang.Integer(occupy(week)(unit))
  }

  def getWeekStateStr(week: Int, unit: Int): String = {
    java.lang.Integer.toBinaryString(occupy(week)(unit))
  }

  def getOccupy(): Array[Array[Int]] = occupy

  def setOccupy(occupy: Array[Array[Int]]) {
    this.occupy = occupy
  }
}
