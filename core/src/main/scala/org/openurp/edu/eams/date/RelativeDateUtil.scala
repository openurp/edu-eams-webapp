package org.openurp.edu.eams.date

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import EamsWeekday._
import RelativeDateUtil._

object RelativeDateUtil {

  def startOn(semester: Semester): RelativeDateUtil = {
    startOn(semester.beginOn, EamsDateUtil.isSundayFirst(semester))
  }

  def startOn(startOn: Date, firstDayOnSunday: Boolean): RelativeDateUtil = {
    val util = new RelativeDateUtil()
    util.startOn = startOn
    util.firstDayOnSunday = firstDayOnSunday
    util.dateUtil = if (firstDayOnSunday) EamsDateUtil.SUNDAY_FIRST else EamsDateUtil.MONDAY_FIRST
    util
  }

  private val formatter = new SimpleDateFormat("yyyy-MM-dd")
}

class RelativeDateUtil private () {

  private var startOn: Date = _

  private var firstDayOnSunday: java.lang.Boolean = _

  private var dateUtil: EamsDateUtil = _

  def getDate(relativeWeekIndex2: Int, weekday: EamsWeekday): Date = {
    val weekOfYear = dateUtil.getWeekOfYear(startOn)
    val relativeWeekIndex = if (relativeWeekIndex2 == 0) 1 else relativeWeekIndex2

    if (relativeWeekIndex < 0) {
      if (Math.abs(relativeWeekIndex) >= weekOfYear) {
        return dateUtil.getDate(EamsDateUtil.getYear(startOn), weekOfYear + relativeWeekIndex - 1, weekday)
      }
      dateUtil.getDate(EamsDateUtil.getYear(startOn), weekOfYear + relativeWeekIndex, weekday)
    } else {
      dateUtil.getDate(EamsDateUtil.getYear(startOn), weekOfYear + relativeWeekIndex - 1, weekday)
    }
  }

  def getDates(relativeWeekIndecies: Array[Int], weekday: EamsWeekday): Array[Date] = {
    val dates = Array.ofDim[Date](relativeWeekIndecies.length)
    for (i <- 0 until relativeWeekIndecies.length) {
      dates(i) = getDate(relativeWeekIndecies(i), weekday)
    }
    dates
  }

  def getDates(relativeWeekIndexString: String, weekday: EamsWeekday): Array[Date] = {
    getDates(Strings.splitToInt(relativeWeekIndexString), weekday)
  }

  def getWeekIndex(date: Date): Int = {
    dateUtil.getNthWeekRelativeFromStart(startOn, date)
  }

  override def toString(): String = {
    Objects.toStringBuilder(this.getClass.getSimpleName)
      .add("startOn", formatter.format(startOn))
      .add("1stDayOnSunday", firstDayOnSunday)
      .toString
  }
}
