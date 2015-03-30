package org.openurp.edu.eams.base.util

class WeekUnit(var cycle: Int, var start: Int, var end: Int) {

  override def toString(): String = (cycle + "[" + start + "-" + end + "]")

}
