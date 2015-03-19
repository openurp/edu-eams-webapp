package org.openurp.edu.eams.teach.exam.service

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.eams.base.util.WeekDays
import org.openurp.edu.eams.base.util.WeekStates
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.eams.date.EamsDateUtil
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.exam.ExamRoom
import org.beangle.commons.lang.time.YearWeekTimeBuilder
import org.beangle.commons.lang.time.WeekDays
import org.openurp.base.Semester



object ExamYearWeekTimeUtil {

  def getYearWeekTimeFromActivity(activity: ExamActivity): YearWeekTime = {
    val f = new SimpleDateFormat("HH:mm")
    val unit = new YearWeekTime()
    unit.setStartTime(getTimeNumber(f.format(activity.getStartAt)))
    unit.setEndTime(getTimeNumber(f.format(activity.getEndAt)))
    val date = activity.getStartAt
    val state = YearWeekTimeBuilder.build(date, WeekStateDirection.LTR)
    unit.setYear(state.year)
    unit.setWeekday(state.day.getIndex)
    unit.newWeekState(state.getString)
    unit
  }

  def getYearWeekTimeFromActivity(examRoom: ExamRoom): YearWeekTime = {
    val f = new SimpleDateFormat("HH:mm")
    val unit = new YearWeekTime()
    unit.setStartTime(getTimeNumber(f.format(examRoom.getStartAt)))
    unit.setEndTime(getTimeNumber(f.format(examRoom.getEndAt)))
    val date = examRoom.getStartAt
    val state = YearWeekTimeBuilder.build(date, WeekStateDirection.LTR)
    unit.setYear(state.year)
    unit.setWeekday(state.day.getIndex)
    unit.newWeekState(state.getString)
    unit
  }

  def buildWeekState(year: Int, from: Int, startWeek: Int): String = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    val weekState = new StringBuffer()
    val weekCount = from + startWeek - 1
    if (weekCount > Semester.OVERALLWEEKS) {
      var newWeekCount = weekCount - Semester.OVERALLWEEKS
      try {
        val newYearFirstDate = sdf.parse(year + 1 + "-01-01")
        val weekDay = getWeekDayByDate(newYearFirstDate)
        if (weekDay != 7) {
          newWeekCount += 1
        }
      } catch {
        case e: ParseException => {
          e.printStackTrace()
          throw new RuntimeException("date parse error!")
        }
      }
      var i = 1
      while (i <= Semester.OVERALLWEEKS) {
        if (newWeekCount == i) {
          weekState.append("1")
        } else {
          weekState.append("0")
        }
        i += 1
      }
    } else {
      var i = 1
      while (i <= Semester.OVERALLWEEKS) {
        if (weekCount == i) {
          weekState.append("1")
        } else {
          weekState.append("0")
        }
        i += 1
      }
    }
    weekState.toString
  }

  def getWeekDayByDate(date: Date): java.lang.Integer = EamsDateUtil.day(date).getIndex

  def getWeekOfYear(date: Date): Int = {
    val gc = new GregorianCalendar()
    gc.setTime(date)
    if (11 == gc.get(Calendar.MONTH) && 1 == gc.get(Calendar.WEEK_OF_YEAR)) {
      gc.getActualMaximum(Calendar.WEEK_OF_YEAR) + 1
    } else {
      gc.get(Calendar.WEEK_OF_YEAR)
    }
  }

  def getDate(semester: Semester, 
      teachWeek: Int, 
      weekDay: Int, 
      times: Array[Int]): Date = {
    var weekday = 0
    weekday = if (weekDay == 7) 1 else weekDay + 1
    val weekStates = Strings.repeat("0", Semester.OVERALLWEEKS).toCharArray()
    weekStates(teachWeek) = '1'
    val weekState = new String(weekStates)
    val yearWeekState = WeekStates.build(semester, weekState, WeekDays.get(weekDay))
      .entrySet()
      .iterator()
      .next()
    val gc = new GregorianCalendar()
    gc.set(Calendar.YEAR, yearWeekState.getKey)
    gc.set(Calendar.WEEK_OF_YEAR, yearWeekState.getValue.indexOf("1") + 1)
    gc.set(Calendar.DAY_OF_WEEK, weekday)
    gc.set(Calendar.HOUR_OF_DAY, times(0))
    gc.set(Calendar.MINUTE, times(1))
    gc.set(Calendar.MILLISECOND, 0)
    gc.getTime
  }

  def getTeachWeekOfYear(semester: Semester, nowDate: Date): Int = {
    val start = Calendar.getInstance
    start.setTime(semester.beginOn)
    start.setFirstDayOfWeek(semester.getFirstWeekday)
    start.set(Calendar.DAY_OF_WEEK, semester.getFirstWeekday)
    var weeks = 0
    while (!start.getTime.after(nowDate)) {
      start.add(Calendar.WEEK_OF_YEAR, 1)
      weeks += 1
    }
    weeks
  }

  @Deprecated
  def getTeachWeekOfYear(fromDate: Date, nowDate: Date): Int = {
    var week = 0
    val fromYear = fromDate.year + 1900
    val nowYear = nowDate.year + 1900
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    try {
      val firstDay = sdf.parse(nowYear + "-01-01")
      val fromWeeks = getWeekOfYear(fromDate)
      val nowWeeks = getWeekOfYear(nowDate)
      val firstDayOfWeek = getWeekDayByDate(firstDay)
      if (nowYear > fromYear) {
        week = 53 - (fromWeeks - 1) + nowWeeks
        if (firstDayOfWeek != 7) {
          week -= 1
        }
      } else {
        week = nowWeeks - (fromWeeks - 1)
      }
    } catch {
      case e: ParseException => {
        e.printStackTrace()
        throw new RuntimeException("date parse error")
      }
    }
    week
  }

  def getTimeNumber(time: String): Int = getTimeNumber(time, ":")

  def getTimeNumber(time: String, delimter: String): Int = {
    val index = time.indexOf(delimter)
    java.lang.Integer.parseInt(time.substring(0, index) + time.substring(index + 1, index + 3))
  }

  def getTimeStr(time: Int): String = getTimeStr(time, ":")

  def convertTime(time: Int): Array[Int] = {
    val times = Array.ofDim[Int](2)
    val strTime = String.valueOf(time)
    if (strTime.length == 3) {
      times(0) = java.lang.Integer.valueOf(strTime.substring(0, 1))
      times(1) = java.lang.Integer.valueOf(strTime.substring(1, 3))
    } else {
      times(0) = java.lang.Integer.valueOf(strTime.substring(0, 2))
      times(1) = java.lang.Integer.valueOf(strTime.substring(2, 4))
    }
    times
  }

  def getTimeStr(time: Int, delimter: String): String = {
    Strings.leftPad(String.valueOf(time / 100), 2, '0') + 
      delimter + 
      Strings.leftPad(String.valueOf(time % 100), 2, '0')
  }
}
