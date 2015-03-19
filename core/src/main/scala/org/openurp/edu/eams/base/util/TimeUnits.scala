package org.openurp.edu.eams.base.util

object YearWeekTimes {

  def toTime(timestring: String): Int = {
    val index = timestring.indexOf(":")
    java.lang.Integer.parseInt(timestring.substring(0, index) + timestring.substring(index + 1, index + 3))
  }

  def toString(time: Int): String = {
    var timestring = String.valueOf(time)
    if (timestring.length < 4) timestring = "0" + timestring
    timestring.substring(0, 2) + ":" + timestring.substring(2)
  }
}
