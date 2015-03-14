package org.openurp.edu.eams.weekstate

import org.openurp.edu.eams.weekstate.SemesterWeekState.RESERVE_BITS
import org.openurp.edu.eams.weekstate.WeekStateDirection.LTR
import org.openurp.edu.eams.weekstate.WeekStateDirection.RTL
import java.util.Arrays
import java.util.Collection
import java.util.Date
import java.util.List
import java.util.Map
import java.util.Map.Entry
import java.util.TreeMap
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.date.EamsDateUtil
import org.openurp.edu.eams.date.EamsWeekday
import org.openurp.edu.eams.date.RelativeDateUtil
import org.openurp.edu.eams.exception.WeekStateException
import YearWeekStateBuilder._

import scala.collection.JavaConversions._

object YearWeekStateBuilder {

  def LTR(year: Int): YearWeekStateBuilder = {
    val helper = new YearWeekStateBuilder()
    helper.year = year
    helper.direction = WeekStateDirection.LTR
    helper.paddingLength = BasicWeekState.MAX_LENGTH
    helper
  }

  protected def RTL(year: Int): YearWeekStateBuilder = {
    val helper = new YearWeekStateBuilder()
    helper.year = year
    helper.direction = WeekStateDirection.RTL
    helper.paddingLength = 0
    helper
  }

  def convertFrom(semesterWeekState: SemesterWeekState): Array[YearWeekState] = {
    if (semesterWeekState == null) {
      return null
    }
    val semester = semesterWeekState.getSemester
    var semesterWeekStateString = semesterWeekState.getString
    if (semesterWeekState.direction == RTL) {
      semesterWeekStateString = new StringBuilder(semesterWeekStateString).reverse()
        .toString
    }
    val weekday = semesterWeekState.getWeekday
    val rdateUtil = RelativeDateUtil.startOn(semester)
    val actual_year2weekIndexOfYearList = new TreeMap[Integer, List[Integer]]()
    var oneIndex = -1
    while (oneIndex < semesterWeekStateString.length) {
      oneIndex = semesterWeekStateString.indexOf('1', oneIndex + 1)
      if (oneIndex == -1) {
        //break
      }
      var sem_weekIndex = oneIndex - RESERVE_BITS
      sem_weekIndex = if (sem_weekIndex >= 0) sem_weekIndex + 1 else sem_weekIndex
      if (sem_weekIndex < -RESERVE_BITS) {
        throw new WeekStateException("Convert Error: weekIndex is less then -" + RESERVE_BITS + 
          " weeks")
      }
      val date = rdateUtil.getDate(sem_weekIndex, weekday)
      val actual_year = EamsDateUtil.getYear(date)
      val actual_weekOfYear = EamsDateUtil.SUNDAY_FIRST.getWeekOfYear(date)
      var actual_year_weekIndecies = actual_year2weekIndexOfYearList.get(java.lang.Integer.valueOf(actual_year))
      if (actual_year_weekIndecies == null) {
        actual_year_weekIndecies = CollectUtils.newArrayList()
        actual_year2weekIndexOfYearList.put(actual_year, actual_year_weekIndecies)
      }
      actual_year_weekIndecies.add(actual_weekOfYear)
    }
    val states = CollectUtils.newArrayList()
    for ((key, value) <- actual_year2weekIndexOfYearList) {
      val year = key
      val year_weekIndecies = value
      states.add(LTR(year).build(year_weekIndecies.toArray(Array.ofDim[Integer](0)), weekday)(0))
    }
    states.toArray(Array.ofDim[YearWeekState](0))
  }

  def merge(state1: YearWeekState, state2: YearWeekState): YearWeekState = {
    if (state1 == null && state2 == null) {
      return null
    }
    if (state1 == null) {
      return state2.clone()
    } else if (state2 == null) {
      return state1.clone()
    }
    if (state1.direction != state2.direction) {
      throw new WeekStateException("Merge Error: Direction Different")
    } else if (state1.getWeekday != state2.getWeekday) {
      throw new WeekStateException("Merge Error: Weekday Different")
    } else if (state1.getYear != state2.getYear) {
      throw new WeekStateException("Merge Error: Year Different")
    }
    val res = new YearWeekState(state1)
    res.setWeekState(state1.getNumber | state2.getNumber)
    res
  }

  def merge(states: Array[YearWeekState]): YearWeekState = {
    if (states == null || states.length == 0) {
      return null
    }
    if (states.length == 1) {
      return states(0).clone()
    }
    var res = states(0)
    for (i <- 1 until states.length) {
      res = merge(res, states(i))
    }
    res
  }

  def merge(states: List[YearWeekState]): YearWeekState = {
    if (CollectUtils.isEmpty(states)) {
      return null
    }
    merge(states.toArray(Array.ofDim[YearWeekState](0)))
  }

  def merge(states: Collection[YearWeekState]): YearWeekState = {
    if (CollectUtils.isEmpty(states)) {
      return null
    }
    merge(states.toArray(Array.ofDim[YearWeekState](0)))
  }

  def parse(weekState: String, direction: WeekStateDirection): Array[Integer] = {
    if (direction == RTL) {
      weekState = new StringBuilder(weekState).reverse().toString
    }
    val weekIndecies = CollectUtils.newArrayList()
    var i = 0
    while (i != -1) {
      i = weekState.indexOf('1', i)
      if (i == -1) {
        //break
      }
      weekIndecies.add(i)
      i = i + 1
    }
    for (j <- 0 until weekIndecies.size) {
      val weekIndex = weekIndecies.get(j)
      weekIndecies.set(j, weekIndex + 1)
    }
    weekIndecies.toArray(Array.ofDim[Integer](0))
  }

  def parse(weekState: java.lang.Long, direction: WeekStateDirection): Array[Integer] = {
    parse(BinaryConverter.toString(weekState), direction)
  }

  def build(date: Date, direction: WeekStateDirection): YearWeekState = {
    val year = EamsDateUtil.getYear(date)
    val weekday = EamsDateUtil.getWeekday(date)
    val weekOfYear = EamsDateUtil.SUNDAY_FIRST.getWeekOfYear(date)
    LTR(year).build(Array(weekOfYear), weekday)(0)
  }
}

class YearWeekStateBuilder private () extends AbsolutWeekStateBuilder() {

  private var year: Int = _

  private var direction: WeekStateDirection = _

  private var paddingLength: java.lang.Integer = _

  def build(weekIndeciesOfYear: Array[Int], weekday: EamsWeekday): Array[YearWeekState] = {
    if (weekIndeciesOfYear == null || weekIndeciesOfYear.length == 0) {
      return null
    }
    val SUNDAY_MODE = EamsDateUtil.SUNDAY_FIRST
    Arrays.sort(weekIndeciesOfYear)
    val actual_year2weekIndexOfYearList = new TreeMap[Integer, List[Integer]]()
    for (weekOfYear <- weekIndeciesOfYear) {
      val date = SUNDAY_MODE.getDate(year, weekOfYear, weekday)
      val actual_weekOfYear = SUNDAY_MODE.getWeekOfYear(date)
      val actual_year = EamsDateUtil.getYear(date)
      var actual_year_weekIndecies = actual_year2weekIndexOfYearList.get(java.lang.Integer.valueOf(actual_year))
      if (actual_year_weekIndecies == null) {
        actual_year_weekIndecies = CollectUtils.newArrayList()
        actual_year2weekIndexOfYearList.put(actual_year, actual_year_weekIndecies)
      }
      actual_year_weekIndecies.add(actual_weekOfYear)
    }
    val states = CollectUtils.newArrayList()
    for ((key, value) <- actual_year2weekIndexOfYearList) {
      val year = key
      val year_weekIndecies = value
      val weekState = new YearWeekState()
      weekState.setYear(year)
      weekState.setDirection(direction)
      weekState.setWeekState(buildString(year_weekIndecies.toArray(Array.ofDim[Integer](0)), year))
      weekState.setWeekday(weekday)
      states.add(weekState)
    }
    states.toArray(Array.ofDim[YearWeekState](0))
  }

  def build(weekIndeciesOfYear: Array[Integer], weekday: EamsWeekday): Array[YearWeekState] = {
    if (weekIndeciesOfYear == null || weekIndeciesOfYear.length == 0) {
      return null
    }
    val weekIndeciesArray = Array.ofDim[Int](weekIndeciesOfYear.length)
    for (i <- 0 until weekIndeciesOfYear.length) {
      weekIndeciesArray(i) = weekIndeciesOfYear(i)
    }
    build(weekIndeciesArray, weekday)
  }

  def build(weekIndeciesOfYear: String, weekday: EamsWeekday): Array[YearWeekState] = {
    if (Strings.isEmpty(weekIndeciesOfYear)) {
      return null
    }
    build(Strings.splitToInt(weekIndeciesOfYear), weekday)
  }

  protected def buildString(weekIndecies: Array[Integer], year: Int): String = {
    val maxWeeks = EamsDateUtil.SUNDAY_FIRST.getWeeksOfYear(year)
    val res = Strings.rightPad("", BasicWeekState.MAX_LENGTH, '0')
    val sb = new StringBuilder(res)
    for (i <- 0 until weekIndecies.length) {
      val weekIndex = weekIndecies(i)
      if (weekIndex <= 0) {
        throw new WeekStateException("Build Error: weekIndex is less than or equal to 0 weeks")
      }
      if (weekIndex > maxWeeks) {
        throw new WeekStateException("Build Error: weekIndex is greater than max weeks of year: " + 
          maxWeeks)
      }
      sb.setCharAt(weekIndex - 1, '1')
    }
    if (this.direction == LTR) {
      sb.toString
    } else {
      Strings.leftPad(sb.reverse().toString.replaceAll("^0+1", "1"), this.paddingLength, '0')
    }
  }

  def parse(weekState: String): Array[Integer] = parse(weekState, this.direction)

  def parse(weekState: java.lang.Long): Array[Integer] = parse(weekState, this.direction)
}
