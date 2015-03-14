package org.openurp.edu.eams.base.util

class WeekUnit(var cycle: Int, var start: Int, var end: Int) {

  override def toString(): String = (cycle + "[" + start + "-" + end + "]")

  def getCycle(): Int = cycle

  def setCycle(cycle: Int) {
    this.cycle = cycle
  }

  def getEnd(): Int = end

  def setEnd(end: Int) {
    this.end = end
  }

  def getStart(): Int = start

  def setStart(start: Int) {
    this.start = start
  }
}
