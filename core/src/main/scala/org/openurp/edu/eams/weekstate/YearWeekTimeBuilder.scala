package org.openurp.edu.eams.weekstate

import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.base.SemesterWeekTime
import org.openurp.edu.eams.date.RelativeDateUtil
import org.openurp.edu.eams.date.EamsDateUtil
import scala.collection.mutable.Buffer
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.time.WeekDays
import org.beangle.commons.lang.time.WeekState
import org.beangle.commons.lang.time.WeekDays.WeekDay
import org.beangle.commons.lang.Strings
import java.util.Arrays
import YearWeekTimeBuilder._

object YearWeekTimeBuilder {

  val MAX_LENGTH = 53

  val RESERVE_BITS = 1

  def convertFrom2(swt: SemesterWeekTime): Array[YearWeekTime] = {
    if (swt == null) return null

    val semester = swt.semester
    var semesterWeekStateString = swt.state.toString
    val weekday = swt.day
    val rdateUtil = RelativeDateUtil.startOn(semester)
    val actual_year2weekIndexOfYearList = Collections.newMap[Integer, Buffer[Integer]]
    var oneIndex = -1
    var break = false
    while (oneIndex < semesterWeekStateString.length && !break) {
      oneIndex = semesterWeekStateString.indexOf('1', oneIndex + 1)
      if (oneIndex == -1) {
        break = true
      } else {
        var sem_weekIndex = oneIndex - RESERVE_BITS
        sem_weekIndex = if (sem_weekIndex >= 0) sem_weekIndex + 1 else sem_weekIndex
        if (sem_weekIndex < -RESERVE_BITS) {
          throw new RuntimeException("Convert Error: weekIndex is less then -" + RESERVE_BITS +
            " weeks")
        }
        val date = rdateUtil.date(sem_weekIndex, weekday)
        val actual_year = EamsDateUtil.year(date)
        val actual_weekOfYear = EamsDateUtil.SUNDAY_FIRST.weekOfYear(date)
        var actual_year_weekIndecies = actual_year2weekIndexOfYearList.get(java.lang.Integer.valueOf(actual_year)).orNull
        if (actual_year_weekIndecies == null) {
          actual_year_weekIndecies = Collections.newBuffer[Integer]
          actual_year2weekIndexOfYearList.put(actual_year, actual_year_weekIndecies)
        }
        actual_year_weekIndecies += actual_weekOfYear
      }
    }
    val states = Collections.newBuffer[YearWeekTime]
    for ((year, year_weekIndecies) <- actual_year2weekIndexOfYearList) {
      states += (new YearWeekTimeBuilder(year).build(year_weekIndecies.toArray, weekday)(0))
    }
    states.toArray
  }

  def merge(state1: YearWeekTime, state2: YearWeekTime): YearWeekTime = {
    if (state1 == null && state2 == null) {
      return null
    }
    if (state1 == null) {
      return new YearWeekTime(state2)
    } else if (state2 == null) {
      return new YearWeekTime(state1)
    }
    if (state1.day != state2.day) {
      throw new RuntimeException("Merge Error: Weekday Different")
    } else if (state1.year != state2.year) {
      throw new RuntimeException("Merge Error: Year Different")
    }
    val res = new YearWeekTime(state1)
    res.state = state1.state | state2.state
    res
  }

  def merge(states: Array[YearWeekTime]): YearWeekTime = {
    if (states == null || states.length == 0) return null

    if (states.length == 1) return new YearWeekTime(states(0))

    var res = states(0)
    for (i <- 1 until states.length) {
      res = merge(res, states(i))
    }
    res
  }

  def merge(states: List[YearWeekTime]): YearWeekTime = {
    if (Collections.isEmpty(states)) return null
    merge(states.toArray)
  }

  def merge(states: Iterable[YearWeekTime]): YearWeekTime = {
    if (Collections.isEmpty(states)) return null
    merge(states.toArray)
  }

  def build(date: java.util.Date): YearWeekTime = {
    val year = EamsDateUtil.year(date)
    val weekday = WeekDays.of(date)
    val weekOfYear = EamsDateUtil.SUNDAY_FIRST.weekOfYear(date)
    new YearWeekTimeBuilder(year).build(Array(Integer.valueOf(weekOfYear)), weekday)(0)
  }
}

class YearWeekTimeBuilder(val year: Int) {

  def build(weekIndeciesOfYear: Array[Integer], weekday: WeekDay): Array[YearWeekTime] = {
    if (weekIndeciesOfYear == null || weekIndeciesOfYear.length == 0) {
      return null
    }
    val SUNDAY_MODE = EamsDateUtil.SUNDAY_FIRST
    weekIndeciesOfYear.sorted

    val actual_year2weekIndexOfYearList = Collections.newMap[Integer, Buffer[Integer]]
    for (weekOfYear <- weekIndeciesOfYear) {
      val date = SUNDAY_MODE.date(year, weekOfYear, weekday)
      val actual_weekOfYear = SUNDAY_MODE.weekOfYear(date)
      val actual_year = EamsDateUtil.year(date)
      var actual_year_weekIndecies = actual_year2weekIndexOfYearList.get(java.lang.Integer.valueOf(actual_year)).orNull
      if (actual_year_weekIndecies == null) {
        actual_year_weekIndecies = Collections.newBuffer[Integer]
        actual_year2weekIndexOfYearList.put(actual_year, actual_year_weekIndecies)
      }
      actual_year_weekIndecies += actual_weekOfYear
    }
    val states = Collections.newBuffer[YearWeekTime]
    for ((year, year_weekIndecies) <- actual_year2weekIndexOfYearList) {
      val weekState = new YearWeekTime()
      weekState.year = year
      weekState.state = buildString(year_weekIndecies.toArray, year)
      weekState.day = weekday
      states += weekState
    }
    states.toArray
  }

  def build(weekIndeciesOfYear: String, weekday: WeekDay): Array[YearWeekTime] = {
    if (Strings.isEmpty(weekIndeciesOfYear)) {
      return null
    }
    build(Strings.splitToInteger(weekIndeciesOfYear), weekday)
  }

  protected def buildString(weekIndecies: Array[Integer], year: Int): WeekState = {
    val maxWeeks = EamsDateUtil.SUNDAY_FIRST.weeksOfYear(year)
    val res = Strings.rightPad("", MAX_LENGTH, '0')
    val sb = new StringBuilder(res)
    for (i <- 0 until weekIndecies.length) {
      val weekIndex = weekIndecies(i)
      if (weekIndex <= 0) {
        throw new RuntimeException("Build Error: weekIndex is less than or equal to 0 weeks")
      }
      if (weekIndex > maxWeeks) {
        throw new RuntimeException("Build Error: weekIndex is greater than max weeks of year: " +
          maxWeeks)
      }
      sb.setCharAt(weekIndex - 1, '1')
    }
    WeekState(Strings.leftPad(sb.toString.replaceAll("^0+1", "1"), /*this.paddingLength*/ 0, '0'))
  }
}
