package org.openurp.edu.eams.weekstate


import java.util.Date
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.eams.date.EamsDateUtil
import org.beangle.commons.lang.time.WeekDays._
import org.openurp.edu.eams.date.RelativeDateUtil
import SemesterWeekTimeBuilder._
import org.openurp.base.SemesterWeekTime
import org.beangle.commons.lang.time.YearWeekTime
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.time.WeekState

object SemesterWeekTimeBuilder {

val RESERVE_BITS =1

val MAX_LENGTH=53

  def RTL(semester: Semester): SemesterWeekTimeBuilder = {
    val helper = new SemesterWeekTimeBuilder()
    helper.semester = semester
    if (semester != null) {
      helper.rdateUtil = RelativeDateUtil.startOn(semester)
    }
    helper
  }

  def merge(state1: SemesterWeekTime, state2: SemesterWeekTime): SemesterWeekTime = {
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
    } else if (state1.semester != state2.semester) {
      throw new RuntimeException("Merge Error: Semester Different")
    }
    val res = new SemesterWeekTime(state1)
    res.weekState=state1.number | state2.number
    res
  }

  def merge(states: Array[SemesterWeekTime]): SemesterWeekTime = {
    if (states == null || states.length == 0) {
      return null
    }
    if (states.length == 1) {
      return states(0)
    }
    var res = states(0)
    for (i <- 1 until states.length) {
      res = merge(res, states(i))
    }
    res
  }

  def merge(states: List[SemesterWeekTime]): SemesterWeekTime = {
    if (Collections.isEmpty(states)) {
      return null
    }
    merge(states.toArray)
  }

  def merge(states: Iterable[SemesterWeekTime]): SemesterWeekTime = {
    if (Collections.isEmpty(states)) {
      return null
    }
    merge(states.toArray)
  }

  def parse(weekState: String): Array[Integer] = {
     // weekState = new StringBuilder(weekState).reverse().toString
    val weekIndecies = Collections.newBuffer[Any][Integer]
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
      var weekIndex = weekIndecies(j)
      weekIndex = if (weekIndex >= RESERVE_BITS) weekIndex - RESERVE_BITS + 1 else weekIndex - RESERVE_BITS
      weekIndecies.update(j, weekIndex)
    }
    weekIndecies.toArray
  }

  def parse(weekState: java.lang.Long): Array[Integer] = {
    parse(BinaryConverter.toString(weekState))
  }
}

class SemesterWeekTimeBuilder{

  private var semester: Semester = _

  private var rdateUtil: RelativeDateUtil = _

  def build(date: Date): SemesterWeekTime = {
    build(Array(rdateUtil.weekIndex(date)), EamsDateUtil.day(date))
  }

  def build(relativeWeekIndecies: Array[Int], weekday: WeekDay): SemesterWeekTime = {
    if (relativeWeekIndecies == null || relativeWeekIndecies.length == 0) {
      return null
    }
    val weekState = new SemesterWeekTime()
    weekState.semester=semester
    weekState.state=buildString(relativeWeekIndecies)
    weekState.day=weekday
    weekState
  }

  def build(relativeWeekIndecies: Array[Integer], weekday: WeekDay): SemesterWeekTime = {
    if (relativeWeekIndecies == null || relativeWeekIndecies.length == 0) {
      return null
    }
    val weekIndeciesArray = Array.ofDim[Int](relativeWeekIndecies.length)
    for (i <- 0 until relativeWeekIndecies.length) {
      weekIndeciesArray(i) = relativeWeekIndecies(i)
    }
    build(weekIndeciesArray, weekday)
  }

  def build(relativeWeekIndecies: String, weekday: WeekDay): SemesterWeekTime = {
    build(Strings.splitToInt(relativeWeekIndecies), weekday)
  }

  private def convertFrom(yearWeekState: YearWeekTime): SemesterWeekTime = {
    if (yearWeekState == null) {
      return null
    }
    var year_weekStateString = new StringBuilder(BinaryConverter.toString(yearWeekState.state.value))
    val weekday = yearWeekState.day
    val sem_weekIndecies = Collections.newBuffer[Any][Integer]
    var oneIndex = -1
    while (oneIndex < year_weekStateString.length) {
      oneIndex = year_weekStateString.indexOf('1', oneIndex + 1)
      if (oneIndex == -1) {
        //break
      }
      val year_weekIndex = oneIndex + 1
      val date = EamsDateUtil.SUNDAY_FIRST.date(yearWeekState.year, year_weekIndex, weekday)
      val sem_weekIndex = rdateUtil.weekIndex(date)
      if (sem_weekIndex < -RESERVE_BITS) {
        throw new RuntimeException("Convert Error: weekIndex is less than -" + RESERVE_BITS + 
          " weeks")
      }
      if (sem_weekIndex > BasicWeekState.MAX_LENGTH) {
        throw new RuntimeException("Convert Error: weekIndex is greater than " + MAX_LENGTH)
      }
      sem_weekIndecies += (sem_weekIndex)
    }
    build(sem_weekIndecies.toArray, yearWeekState.day)
  }

  def convertFrom(yearWeekStates: Array[YearWeekTime]): SemesterWeekTime = {
    if (yearWeekStates == null || yearWeekStates.length == 0) {
      return null
    }
    val weekday = yearWeekStates(0).day
    for (i <- 1 until yearWeekStates.length if weekday != yearWeekStates(i).day) {
      throw new RuntimeException("Convert error: weekday should be same")
    }
    val states = Collections.newBuffer[Any][SemesterWeekTime]
    for (i <- 0 until yearWeekStates.length) {
      val state = convertFrom(yearWeekStates(i))
      if (state != null) {
        states += state
      }
    }
    if (Collections.isEmpty(states)) {
      return null
    }
    val state = states(0)
    var number = state.state.value
    for (i <- 1 until states.size) {
      number = number | states(i).state.value
    }
    state.state=new WeekState(number)
    state
  }

  def convertFrom(yearWeekStates: List[YearWeekTime]): SemesterWeekTime = {
    convertFrom(yearWeekStates.toArray)
  }

  def convertFrom(yearWeekStates: Iterable[YearWeekTime]): SemesterWeekTime = {
    convertFrom(yearWeekStates.toArray)
  }

  protected def buildString(weekIndecies: Array[Int]): String = {
    val res = Strings.repeat("0", MAX_LENGTH)
    val originCharAt = RESERVE_BITS
    val sb = new StringBuilder(res)
    for (i <- 0 until weekIndecies.length) {
      val weekIndex = weekIndecies(i)
      var charAt = 0
      charAt = if (weekIndex <= 0) originCharAt + weekIndex else originCharAt + weekIndex - 1
      if (charAt < 0) {
        throw new RuntimeException("WeekIndex is less then -" + RESERVE_BITS + " weeks")
      }
      if (charAt < RESERVE_BITS) {
        sb.setCharAt(charAt, '1')
      } else {
        if (charAt + 1 > sb.length) {
          sb.append(Strings.repeat('0', charAt + 1 - sb.length))
        }
        sb.setCharAt(charAt, '1')
      }
    }
      Strings.leftPad(sb.toString.replaceAll("^0+1", "1"), /*this.paddingLength*/0, '0')
  }

  def parse(weekState: String): Array[Integer] = parse(weekState)

  def parse(weekState: java.lang.Long): Array[Integer] = parse(weekState)
}
