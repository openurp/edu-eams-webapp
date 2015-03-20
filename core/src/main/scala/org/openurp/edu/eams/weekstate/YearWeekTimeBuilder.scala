package org.openurp.edu.eams.weekstate

import java.util.{Arrays, Date, TreeMap}

import collection.mutable.Buffer

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.WeekDays._
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.base.{Semester, SemesterWeekTime}
import org.openurp.edu.eams.date.{EamsDateUtil, RelativeDateUtil}

import YearWeekTimeBuilder._


object YearWeekTimeBuilder {
  
val MAX_LENGTH=53

val RESERVE_BITS =1

  def RTL(year: Int): YearWeekTimeBuilder = {
    val helper = new YearWeekTimeBuilder()
    helper.year = year
    helper
  }

  def convertFrom(semesterWeekState: SemesterWeekTime): Array[YearWeekTime] = {
    if (semesterWeekState == null) {
      return null
    }
    val semester = semesterWeekState.semester
    var semesterWeekStateString = semesterWeekState.state.toString()
    val weekday = semesterWeekState.day
    val rdateUtil = RelativeDateUtil.startOn(semester)
    val actual_year2weekIndexOfYearList = new TreeMap[Integer, Buffer[Integer]]()
    var oneIndex = -1
    while (oneIndex < semesterWeekStateString.length) {
      oneIndex = semesterWeekStateString.indexOf('1', oneIndex + 1)
      if (oneIndex == -1) {
        //break
      }
      var sem_weekIndex = oneIndex - RESERVE_BITS
      sem_weekIndex = if (sem_weekIndex >= 0) sem_weekIndex + 1 else sem_weekIndex
      if (sem_weekIndex < -RESERVE_BITS) {
        throw new RuntimeException("Convert Error: weekIndex is less then -" + RESERVE_BITS + 
          " weeks")
      }
      val date = rdateUtil.date(sem_weekIndex, weekday)
      val actual_year = EamsDateUtil.year(date)
      val actual_weekOfYear = EamsDateUtil.SUNDAY_FIRST.weekOfYear(date)
      var actual_year_weekIndecies = actual_year2weekIndexOfYearList.get(java.lang.Integer.valueOf(actual_year))
      if (actual_year_weekIndecies == null) {
        actual_year_weekIndecies = CollectUtils.newArrayList[Integer]
        actual_year2weekIndexOfYearList.put(actual_year, actual_year_weekIndecies)
      }
      actual_year_weekIndecies += actual_weekOfYear
    }
    val states = CollectUtils.newArrayList[YearWeekTime]
     val iter  = actual_year2weekIndexOfYearList.entrySet().iterator()
    while(iter.hasNext()){
      val entry = iter.next()
      val year = entry.getKey.asInstanceOf[Int]
      val year_weekIndecies = entry.getValue()
      states += (RTL(year).build(year_weekIndecies.toArray, weekday)(0))
    }
    states.toArray
  }

  def merge(state1: YearWeekTime, state2: YearWeekTime): YearWeekTime = {
    if (state1 == null && state2 == null) {
      return null
    }
    if (state1 == null) {
      return state2.clone()
    } else if (state2 == null) {
      return state1.clone()
    }
     if (state1.day != state2.day) {
      throw new RuntimeException("Merge Error: Weekday Different")
    } else if (state1.year != state2.year) {
      throw new RuntimeException("Merge Error: Year Different")
    }
    val res = new YearWeekTime(state1)
    res.weekState=state1.number | state2.number
    res
  }

  def merge(states: Array[YearWeekTime]): YearWeekTime = {
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

  def merge(states: List[YearWeekTime]): YearWeekTime = {
    if (CollectUtils.isEmpty(states)) {
      return null
    }
    merge(states.toArray)
  }

  def merge(states: Iterable[YearWeekTime]): YearWeekTime = {
    if (CollectUtils.isEmpty(states)) {
      return null
    }
    merge(states.toArray)
  }

  def parse(ws: String): Array[Integer] = {
      val weekState = new StringBuilder(ws).toString
    val weekIndecies = CollectUtils.newArrayList[Integer]
    var i = 0
    while (i != -1) {
      i = weekState.indexOf('1', i)
      if (i == -1) {
        //break
      }
      weekIndecies += i
      i = i + 1
    }
    for (j <- 0 until weekIndecies.size) {
      val weekIndex = weekIndecies(j)
      weekIndecies.update(j, weekIndex + 1)
    }
    weekIndecies.toArray
  }

  def parse(weekState: java.lang.Long): Array[Integer] = {
    parse(BinaryConverter.toString(weekState))
  }

  def build(date: Date): YearWeekTime = {
    val year = EamsDateUtil.year(date)
    val weekday = rDateUtil.day(date)
    val weekOfYear = EamsDateUtil.SUNDAY_FIRST.weekOfYear(date)
    RTL(year).build(Array(weekOfYear), weekday)(0)
  }
}

class YearWeekTimeBuilder {

  private var year: Int = _

  def build(weekIndeciesOfYear: Array[Int], weekday: WeekDay): Array[YearWeekTime] = {
    if (weekIndeciesOfYear == null || weekIndeciesOfYear.length == 0) {
      return null
    }
    val SUNDAY_MODE = EamsDateUtil.SUNDAY_FIRST
    Arrays.sort(weekIndeciesOfYear)
    val actual_year2weekIndexOfYearList = new TreeMap[Integer, Buffer[Integer]]()
    for (weekOfYear <- weekIndeciesOfYear) {
      val date = SUNDAY_MODE.date(year, weekOfYear, weekday)
      val actual_weekOfYear = SUNDAY_MODE.weekOfYear(date)
      val actual_year = EamsDateUtil.year(date)
      var actual_year_weekIndecies = actual_year2weekIndexOfYearList.get(java.lang.Integer.valueOf(actual_year))
      if (actual_year_weekIndecies == null) {
        actual_year_weekIndecies = CollectUtils.newArrayList[Integer]
        actual_year2weekIndexOfYearList.put(actual_year, actual_year_weekIndecies)
      }
      actual_year_weekIndecies += actual_weekOfYear
    }
    val states = CollectUtils.newArrayList[YearWeekTime]
    val iter  = actual_year2weekIndexOfYearList.entrySet().iterator()
    while(iter.hasNext()){
      val entry = iter.next()
      val year = entry.getKey.asInstanceOf[Int]
      val year_weekIndecies = entry.getValue
      val weekState = new YearWeekTime()
      weekState.year=year
      weekState.state=buildString(year_weekIndecies.toArray, year)
      weekState.day=weekday
      states += weekState
    }
    states.toArray
  }

  def build(weekIndeciesOfYear: Array[Integer], weekday: WeekDay): Array[YearWeekTime] = {
    if (weekIndeciesOfYear == null || weekIndeciesOfYear.length == 0) {
      return null
    }
    val weekIndeciesArray = Array.ofDim[Int](weekIndeciesOfYear.length)
    for (i <- 0 until weekIndeciesOfYear.length) {
      weekIndeciesArray(i) = weekIndeciesOfYear(i)
    }
    build(weekIndeciesArray, weekday)
  }

  def build(weekIndeciesOfYear: String, weekday: WeekDay): Array[YearWeekTime] = {
    if (Strings.isEmpty(weekIndeciesOfYear)) {
      return null
    }
    build(Strings.splitToInt(weekIndeciesOfYear), weekday)
  }

  protected def buildString(weekIndecies: Array[Integer], year: Int): String = {
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
      Strings.leftPad(sb.toString.replaceAll("^0+1", "1"), /*this.paddingLength*/0, '0')
  }

  def parse(weekState: String): Array[Integer] = parse(weekState)

  def parse(weekState: java.lang.Long): Array[Integer] = parse(weekState)
}
