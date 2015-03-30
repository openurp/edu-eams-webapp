package org.openurp.edu.eams.weekstate

import java.util.Calendar
import java.util.GregorianCalendar

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.WeekDays.Sun
import org.beangle.commons.lang.time.WeekDays.WeekDay
import org.beangle.commons.lang.time.WeekState
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.base.Semester
import org.openurp.edu.eams.number.NumberRangeDigestor

object WeekStates {

  val OVERALLWEEKS = 53
  val RESERVE_BITS = 1

  def relativeMerge(units: Iterable[YearWeekTime], semester: Semester): String = {
    var thisYearWeeks = Strings.repeat("0", OVERALLWEEKS)
    var nextYearWeeks = ""
    val thisYear = getStartYear(semester)
    val first = units.iterator.next()
    for (unit <- units) {
      if (unit.year == thisYear) {
        //FIXME
        thisYearWeeks = unit.state.toString()
      } else if (unit.year == thisYear + 1) {
        //FIXME
        nextYearWeeks = unit.state.toString()
      }
    }
    var weekStates: String = null
    if (!shareAt53(thisYear)) {
      weekStates = thisYearWeeks + nextYearWeeks
    } else {
      var shorten = false
      if (thisYearWeeks.length > 0 && thisYearWeeks.charAt(52) == '0') {
        thisYearWeeks = thisYearWeeks.substring(0, 52)
        shorten = true
      }
      if (!shorten && nextYearWeeks.length > 0 && nextYearWeeks.charAt(0) == '0') {
        nextYearWeeks = nextYearWeeks.substring(1)
      }
      weekStates = thisYearWeeks + nextYearWeeks
    }
    var weekday = first.day.index
    if (semester.firstWeekday != Sun && weekday < semester.firstWeekday.index) {
      weekStates = weekStates.substring(1)
    }
    weekStates
  }

  def build(str: String): WeekState = {
    var newstr = Strings.replace(str, "，", ",")
    newstr = Strings.replace(newstr, "－", "-")
    val weekPairs = Strings.split(newstr, ",")
    val sb = new StringBuffer(Strings.repeat("0", OVERALLWEEKS))
    for (weekPair <- weekPairs) {
      if (Strings.contains(weekPair, "-")) {
        var cycle = 0
        if (weekPair.indexOf('单') != -1) {
          cycle = 1
        } else if (weekPair.indexOf('双') != -1) {
          cycle = 2
        }
        val new_weekPair = weekPair.replaceAll("[^\\d-]", "")
        val startWeek = Strings.substringBefore(new_weekPair, "-")
        val endWeek = Strings.substringAfter(new_weekPair, "-")
        if (Numbers.isDigits(startWeek) && Numbers.isDigits(endWeek)) {
          var i = Numbers.toInt(startWeek)
          while (i <= Numbers.toInt(endWeek)) {
            if (cycle == 0) {
              sb.setCharAt(i, '1')
            } else if (cycle == 1) {
              if (i % 2 == 1) sb.setCharAt(i, '1')
            } else {
              if (i % 2 == 0) sb.setCharAt(i, '1')
            }
            i += 1
          }
        }
      } else {
        if (Numbers.isDigits(weekPair)) {
          sb.setCharAt(Numbers.toInt(weekPair), '1')
        }
      }
    }
    //FIXME
    WeekState(sb.toString)
  }

  def build(semester: Semester, startWeek: Int, endWeek: Int, weekDay: WeekDay): Map[Integer, String] = {
    val start = startWeek
    var end = endWeek
    if (end >= OVERALLWEEKS) end = OVERALLWEEKS - 1
    val weekStates = Strings.repeat("0", OVERALLWEEKS).toCharArray()
    var i = start
    while (i <= end) {
      weekStates(i) = '1'
      i += 1
    }
    build(semester, new String(weekStates), weekDay)
  }

  def build(semester: Semester, weekState: String, weekDay: WeekDay): Map[Integer, String] = {
    if (semester == null) return Map.empty
    val year = getStartYear(semester)
    val sb = new StringBuffer(Strings.repeat("0", semester.startWeek(Sun) - 1))
    sb.append(weekState.substring(1)).append(Strings.repeat("0", OVERALLWEEKS * 2 - sb.length))
    if (weekDay == Sun && semester.firstWeekday.index > Calendar.SUNDAY) sb.insert(0, '0')
    if (shareAt53(year)) {
      var weekday = weekDay.id + 1
      if (weekday == 8) weekday = 1
      if (weekday <= lastWeekday(year)) {
        sb.insert(OVERALLWEEKS, "0")
      } else {
        sb.insert(OVERALLWEEKS - 1, "0")
      }
    }
    val weekStates = new collection.mutable.HashMap[Integer, String]
    if (sb.substring(0, OVERALLWEEKS).indexOf("1") !=
      -1) {
      weekStates.put(year, sb.substring(0, OVERALLWEEKS))
    }
    if (sb.substring(OVERALLWEEKS, 2 * OVERALLWEEKS)
      .indexOf("1") !=
      -1) {
      weekStates.put(year + 1, sb.substring(OVERALLWEEKS, 2 * OVERALLWEEKS))
    }
    weekStates.toMap
  }

  private def shareAt53(year: Int): Boolean = {
    val lastDay = year + "-12-31"
    val gregorianCalendar = new GregorianCalendar()
    gregorianCalendar.setTime(java.sql.Date.valueOf(lastDay))
    (gregorianCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
  }

  private def lastWeekday(year: Int): Int = {
    val lastDay = year + "-12-31"
    val gregorianCalendar = new GregorianCalendar()
    gregorianCalendar.setTime(java.sql.Date.valueOf(lastDay))
    gregorianCalendar.get(Calendar.DAY_OF_WEEK)
  }

  private def getStartYear(semester: Semester): Int = {
    if (null != semester.beginOn) {
      val gc = new GregorianCalendar()
      gc.setTime(semester.beginOn)
      return gc.get(Calendar.YEAR)
    }
    0
  }

  def digest(state: WeekState): String = {
    if (null == state) return ""
    val weekIndecies = parse(state.toString /**FIXME*/ )
    val digest = NumberRangeDigestor.digest(weekIndecies, null)
    digest.replace("[", "").replace("]", "").replace("number.range.odd", "单")
      .replace("number.range.even", "双")
  }

  def parse(weekState: String): Array[Integer] = {
    // weekState = new StringBuilder(weekState).reverse().toString
    val weekIndecies = Collections.newBuffer[Integer]
    var i = 0
    while (i != -1) {
      i = weekState.indexOf('1', i)
      if (i != -1) {
        weekIndecies += i
        i = i + 1
      }
    }
    for (j <- 0 until weekIndecies.size) {
      var weekIndex = weekIndecies(j)
      weekIndex = if (weekIndex >= RESERVE_BITS) weekIndex - RESERVE_BITS + 1 else weekIndex - RESERVE_BITS
      weekIndecies.update(j, weekIndex)
    }
    weekIndecies.toArray
  }

}
