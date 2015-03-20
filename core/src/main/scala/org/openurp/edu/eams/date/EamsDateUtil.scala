package org.openurp.edu.eams.date

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.Weeks
import org.openurp.base.Semester
import org.openurp.edu.eams.base.util.WeekStates
import org.beangle.commons.lang.time.WeekDays._
import org.beangle.commons.lang.time.WeekDays
object EamsDateUtil {

  val SUNDAY_FIRST = new EamsDateUtil(true)

  val MONDAY_FIRST = new EamsDateUtil(false)

  def isSundayFirst(semester: Semester): Boolean = {
    semester.firstWeekday  == Sun
  }

  def getWeekday(date: Date): WeekDay = {
    WeekDays.of(date)
  }

  def year(date: Date): Int = {
    new DateTime(date).year.get
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

  def lastWeekdayOfYear(year: Int): WeekDay = {
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

  def isBefore(weekday1: WeekDay, weekday2: WeekDay): Boolean = {
    isBefore(weekday1, weekday2, firstDayOnSunday)
  }

  private def isBefore(weekday1: WeekDay, weekday2: WeekDay, firstDayOnSunday: Boolean): Boolean = {
    if (weekday1 == null || weekday2 == null) {
      return false
    }
    var w1 = weekday1.id
    var w2 = weekday2.id
    if (firstDayOnSunday) {
      w1 = weekday1.index
      w2 = weekday2.index
    }
    w1 - w2 < 0
  }

  def isAfter(weekday1: WeekDay, weekday2: WeekDay): Boolean = {
    isAfter(weekday1, weekday2, firstDayOnSunday)
  }

  private def isAfter(weekday1: WeekDay, weekday2: WeekDay, firstDayOnSunday: Boolean): Boolean = {
    if (weekday1 == null || weekday2 == null) {
      return false
    }
    var w1 = weekday1.id
    var w2 = weekday2.id
    if (firstDayOnSunday) {
      w1 = weekday1.index
      w2 = weekday2.index
    }
    w1 - w2 > 0
  }

  def getWeekOfYearOfLastDay(year: Int): Int = {
    getWeekOfYearOfLastDay(year, firstDayOnSunday)
  }

  private def getWeekOfYearOfLastDay(year: Int, firstDayOnSunday: Boolean): Int = {
    nthWeekRelativeFromStart(new DateTime(year, 1, 1, 0, 0).toDate(), new DateTime(year, 12, 31, 0,
      0).toDate(), firstDayOnSunday)
  }

  def date(year: Int, weekOfYear: Int, weekday: WeekDay): java.util.Date = {
    date(year, weekOfYear, weekday, firstDayOnSunday)
  }

  private def date(year: Int, weekOfYear: Int, weekday: WeekDay, firstDayOnSunday: Boolean): java.util.Date = {
    val calendar = buildCalendar(firstDayOnSunday)
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.WEEK_OF_YEAR, 1)
    calendar.set(Calendar.DAY_OF_WEEK, weekday.index)
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

  def weekOfYear(date: Date): Int = getWeekOfYear(date, firstDayOnSunday)

  private def getWeekOfYear(date: Date, firstDayOnSunday: Boolean): Int = {
    val year = year(date)
    val firstDayOfYear = java.sql.Date.valueOf("" + year + "-01-01")
    nthWeekRelativeFromStart(firstDayOfYear, date)
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
    val weeks = Weeks.weeksBetween(new Instant(firstDayOfWeekOfStartDate), new Instant(max)).getWeeks
    if (start.after(end)) -weeks else weeks
  }

  def nthWeekRelativeFromStart(start: java.util.Date, end: java.util.Date): Int = {
    nthWeekRelativeFromStart(start, end, firstDayOnSunday)
  }

  private def nthWeekRelativeFromStart(start: java.util.Date, end: java.util.Date, firstDayOnSunday: Boolean): Int = {
    val weeksBetween = getWeeksBetween(start, end, firstDayOnSunday)
    if (weeksBetween >= 0) {
      return weeksBetween + 1
    }
    weeksBetween
  }

  def weeksOfYear(year: Int): Int = {
    nthWeekRelativeFromStart(java.sql.Date.valueOf("" + year + "-01-01"), java.sql.Date.valueOf("" + year + "-12-31"))
  }

  def isLastDayOfYearAlsoLastDayOfWeek(year: Int): Boolean = {
    isLastDayOfYearAlsoLastDayOfWeek(year, firstDayOnSunday)
  }

  private def isLastDayOfYearAlsoLastDayOfWeek(year: Int, firstDayOnSunday: Boolean): Boolean = {
    if (firstDayOnSunday) {
      lastWeekdayOfYear(year) == Sat
    } else {
      lastWeekdayOfYear(year) == Sun
    }
  }

  def getWeekdayArray(): Array[WeekDay] = {
    WeekDays.values.toArray.asInstanceOf[Array[WeekDay]]
  }

  def getWeekdayList(): List[WeekDay] = {
    WeekDays.values.toList.asInstanceOf[List[WeekDay]]
  }

  override def toString(): String = {
    if (this == SUNDAY_FIRST) {
      return "SUNDAY_FIRST"
    }
    "MONDAY_FIRST"
  }
}
