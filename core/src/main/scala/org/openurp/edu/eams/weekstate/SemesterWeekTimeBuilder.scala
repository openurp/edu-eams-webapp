package org.openurp.edu.eams.weekstate

import java.util.Date
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.time.WeekDays.WeekDay
import org.beangle.commons.lang.time.WeekDays
import org.beangle.commons.lang.time.YearWeekTime
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.time.WeekState
import org.openurp.base.Semester
import org.openurp.edu.eams.date.EamsDateUtil
import org.openurp.edu.eams.date.RelativeDateUtil
import org.openurp.base.SemesterWeekTime
import SemesterWeekTimeBuilder._

object SemesterWeekTimeBuilder {

  val RESERVE_BITS = 1

  val MAX_LENGTH = 53

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
    res.state = state1.state | state2.state
    res
  }

  def merge(states: Array[SemesterWeekTime]): SemesterWeekTime = {
    if (states == null || states.length == 0) return null
    if (states.length == 1) return states(0)
    var res = states(0)
    for (i <- 1 until states.length) {
      res = merge(res, states(i))
    }
    res
  }

  def merge(states: List[SemesterWeekTime]): SemesterWeekTime = {
    if (Collections.isEmpty(states)) return null
    merge(states.toArray)
  }

  def merge(states: Iterable[SemesterWeekTime]): SemesterWeekTime = {
    if (Collections.isEmpty(states)) return null
    merge(states.toArray)
  }
}

class SemesterWeekTimeBuilder(val semester: Semester) {

  private var rdateUtil: RelativeDateUtil = _

  if (null != semester) {
    rdateUtil = RelativeDateUtil.startOn(semester)
  }

  def build(date: Date): SemesterWeekTime = {
    build(Array(rdateUtil.weekIndex(date)), WeekDays.of(date))
  }

  def build(relativeWeekIndecies: Array[Int], weekday: WeekDay): SemesterWeekTime = {
    if (relativeWeekIndecies == null || relativeWeekIndecies.length == 0) {
      return null
    }
    val weekState = new SemesterWeekTime()
    weekState.semester = semester
    weekState.state = buildString(relativeWeekIndecies)
    weekState.day = weekday
    weekState
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
    val sem_weekIndecies = Collections.newBuffer[Int]
    var oneIndex = -1
    var break = false
    while (oneIndex < year_weekStateString.length && !break) {
      oneIndex = year_weekStateString.indexOf('1', oneIndex + 1)
      if (oneIndex == -1) {
        break = true
      } else {
        val year_weekIndex = oneIndex + 1
        val date = EamsDateUtil.SUNDAY_FIRST.date(yearWeekState.year, year_weekIndex, weekday)
        val sem_weekIndex = rdateUtil.weekIndex(date)
        if (sem_weekIndex < -RESERVE_BITS) {
          throw new RuntimeException("Convert Error: weekIndex is less than -" + RESERVE_BITS +
            " weeks")
        }
        if (sem_weekIndex > MAX_LENGTH) {
          throw new RuntimeException("Convert Error: weekIndex is greater than " + MAX_LENGTH)
        }
        sem_weekIndecies += (sem_weekIndex)
      }
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
    val states = Collections.newBuffer[SemesterWeekTime]
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
    state.state = new WeekState(number)
    state
  }

  def convertFrom(yearWeekStates: Iterable[YearWeekTime]): SemesterWeekTime = {
    convertFrom(yearWeekStates.toArray)
  }

  protected def buildString(weekIndecies: Array[Int]): WeekState = {
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
    WeekState(Strings.leftPad(sb.toString.replaceAll("^0+1", "1"), /*this.paddingLength*/ 0, '0'))
  }

}
