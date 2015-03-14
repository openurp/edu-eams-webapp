package org.openurp.edu.eams.date

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.Weeks
import org.openurp.base.Semester
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.eams.date.EamsWeekday._

object EamsDateUtil {

  val SUNDAY_FIRST = new EamsDateUtil(true)

  val MONDAY_FIRST = new EamsDateUtil(false)

  def isSundayFirst(semester: Semester): Boolean = {
    WeekStates.jdkWeekIdex(semester.firstWeekday) == EamsWeekday.SUNDAY
  }

  def getWeekday(date: Date): EamsWeekday = {
    EamsWeekday.getDay(new DateTime(date).getDayOfWeek)
  }

  def getYear(date: Date): Int = {
    new DateTime(date).getYear
  }

  def getDatesOfThatWeek(date: Date, firstDayOnSunday: Boolean): List[Date] = {
    val dates = new collection.mutable.ListBuffer[Date]
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    if (firstDayOnSunday) {
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
      dates += calendar.getTime
    } else {
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
      dates += calendar.getTime
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
      dates += calendar.getTime
      val sunday = Calendar.getInstance
      sunday.setTime(dates.head)
      sunday.add(Calendar.DAY_OF_WEEK, 6)
      dates += sunday.getTime
    }
    dates.toList
  }

  def getLastWeekdayOfYear(year: Int): EamsWeekday = {
    getWeekday(java.sql.Date.valueOf("" + year + "-12-31"))
  }

  private def buildCalendar(firstDayOnSunday: Boolean): Calendar = {
    val calendar = Calendar.getInstance()
    val firstDayOfWeek = if (firstDayOnSunday) Calendar.SUNDAY else Calendar.MONDAY
    calendar.setMinimalDaysInFirstWeek(1)
    calendar.setFirstDayOfWeek(firstDayOfWeek)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar
  }

  private def buildCalendar(firstDayOnSunday: Boolean, time: Date): Calendar = {
    val calendar = buildCalendar(firstDayOnSunday)
    calendar.setTime(time)
    calendar
  }
}

import EamsDateUtil._
class EamsDateUtil (var firstDayOnSunday: Boolean) {

  def isBefore(weekday1: EamsWeekday, weekday2: EamsWeekday): Boolean = {
    isBefore(weekday1, weekday2, firstDayOnSunday)
  }

  private def isBefore(weekday1: EamsWeekday, weekday2: EamsWeekday, firstDayOnSunday: Boolean): Boolean = {
    if (weekday1 == null || weekday2 == null) {
      return false
    }
    var w1 = weekday1.index
    var w2 = weekday2.index
    if (firstDayOnSunday) {
      w1 = weekday1.getJdkIndex
      w2 = weekday2.getJdkIndex
    }
    w1 - w2 < 0
  }

  def isAfter(weekday1: EamsWeekday, weekday2: EamsWeekday): Boolean = {
    isAfter(weekday1, weekday2, firstDayOnSunday)
  }

  private def isAfter(weekday1: EamsWeekday, weekday2: EamsWeekday, firstDayOnSunday: Boolean): Boolean = {
    if (weekday1 == null || weekday2 == null) {
      return false
    }
    var w1 = weekday1.index
    var w2 = weekday2.index
    if (firstDayOnSunday) {
      w1 = weekday1.getJdkIndex
      w2 = weekday2.getJdkIndex
    }
    w1 - w2 > 0
  }

  def getWeekOfYearOfLastDay(year: Int): Int = {
    getWeekOfYearOfLastDay(year, firstDayOnSunday)
  }

  private def getWeekOfYearOfLastDay(year: Int, firstDayOnSunday: Boolean): Int = {
    getNthWeekRelativeFromStart(new DateTime(year, 1, 1, 0, 0).toDate(), new DateTime(year, 12, 31, 0,
      0).toDate(), firstDayOnSunday)
  }

  def getDate(year: Int, weekOfYear: Int, weekday: EamsWeekday): java.util.Date = {
    getDate(year, weekOfYear, weekday, firstDayOnSunday)
  }

  private def getDate(year: Int, weekOfYear: Int, weekday: EamsWeekday, firstDayOnSunday: Boolean): java.util.Date = {
    val calendar = buildCalendar(firstDayOnSunday)
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.WEEK_OF_YEAR, 1)
    calendar.set(Calendar.DAY_OF_WEEK, weekday.getJdkIndex)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    if (weekOfYear > 0) {
      new DateTime(calendar).plusDays((weekOfYear - 1) * 7)
        .toDate()
    } else if (weekOfYear == 0) {
      new DateTime(calendar).toDate()
    } else {
      new DateTime(calendar).plusDays((weekOfYear) * 7).toDate()
    }
  }

  def getWeekOfYear(date: Date): Int = getWeekOfYear(date, firstDayOnSunday)

  private def getWeekOfYear(date: Date, firstDayOnSunday: Boolean): Int = {
    val year = getYear(date)
    val firstDayOfYear = java.sql.Date.valueOf("" + year + "-01-01")
    getNthWeekRelativeFromStart(firstDayOfYear, date)
  }

  def getWeeksBetween(start: java.util.Date, end: java.util.Date): Int = {
    getWeeksBetween(start, end, firstDayOnSunday)
  }

  private def getWeeksBetween(start: java.util.Date, end: java.util.Date, firstDayOnSunday: Boolean): Int = {
    val min = if (start.after(end)) end else start
    val max = if (start.after(end)) start else end
    val firstDayOfWeekOfStartDate = buildCalendar(firstDayOnSunday, min)
    val firstDayOfWeek = if (firstDayOnSunday) Calendar.SUNDAY else Calendar.MONDAY
    firstDayOfWeekOfStartDate.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    val weeks = Weeks.weeksBetween(new Instant(firstDayOfWeekOfStartDate), new Instant(max))
      .getWeeks
    if (start.after(end)) -weeks else weeks
  }

  def getNthWeekRelativeFromStart(start: java.util.Date, end: java.util.Date): Int = {
    getNthWeekRelativeFromStart(start, end, firstDayOnSunday)
  }

  private def getNthWeekRelativeFromStart(start: java.util.Date, end: java.util.Date, firstDayOnSunday: Boolean): Int = {
    val weeksBetween = getWeeksBetween(start, end, firstDayOnSunday)
    if (weeksBetween >= 0) {
      return weeksBetween + 1
    }
    weeksBetween
  }

  def getWeeksOfYear(year: Int): Int = {
    getNthWeekRelativeFromStart(java.sql.Date.valueOf("" + year + "-01-01"), java.sql.Date.valueOf("" + year + "-12-31"))
  }

  def isLastDayOfYearAlsoLastDayOfWeek(year: Int): Boolean = {
    isLastDayOfYearAlsoLastDayOfWeek(year, firstDayOnSunday)
  }

  private def isLastDayOfYearAlsoLastDayOfWeek(year: Int, firstDayOnSunday: Boolean): Boolean = {
    if (firstDayOnSunday) {
      getLastWeekdayOfYear(year) == EamsWeekday.SATURDAY
    } else {
      getLastWeekdayOfYear(year) == EamsWeekday.SUNDAY
    }
  }

  def getWeekdayArray(): Array[EamsWeekday] = {
    EamsWeekday.getWeekdayArray(firstDayOnSunday)
  }

  def getWeekdayList(): List[EamsWeekday] = {
    EamsWeekday.getWeekdayList(firstDayOnSunday)
  }

  override def toString(): String = {
    if (this == SUNDAY_FIRST) {
      return "SUNDAY_FIRST"
    }
    "MONDAY_FIRST"
  }
}
