package org.openurp.edu.eams.teach.exam.service

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.eams.weekstate.WeekStates
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.eams.date.EamsDateUtil
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.exam.ExamRoom
import org.beangle.commons.lang.time.WeekDays
import org.beangle.commons.lang.time.WeekDays._
import org.openurp.base.Semester
import org.openurp.edu.eams.weekstate.YearWeekTimeBuilder
import org.openurp.base.Semester

object ExamYearWeekTimeUtil {

  val OVERALLWEEKS =53
  
  def getYearWeekTimeFromActivity(activity: ExamActivity): YearWeekTime = {
    val f = new SimpleDateFormat("HH:mm")
    val unit = new YearWeekTime()
    unit.begin=activity.beginAt
    unit.end=activity.endAt
    val date = activity.examOn
    val state = YearWeekTimeBuilder.build(date)
    unit.year=state.year
    unit.day=state.day
    unit
  }

  def getYearWeekTimeFromActivity(examRoom: ExamRoom): YearWeekTime = {
    val f = new SimpleDateFormat("HH:mm")
    val unit = new YearWeekTime()
    unit.begin=examRoom.beginAt
    unit.end=examRoom.endAt
    val date = examRoom.examOn
    val state = YearWeekTimeBuilder.build(date)
    unit.year= state.year
    unit.day= state.day
    unit
  }

  def buildWeekState(year: Int, from: Int, startWeek: Int): String = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    val weekState = new StringBuffer()
    val weekCount = from + startWeek - 1
    if (weekCount > ExamYearWeekTimeUtil.OVERALLWEEKS) {
      var newWeekCount = weekCount - ExamYearWeekTimeUtil.OVERALLWEEKS
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
      while (i <= ExamYearWeekTimeUtil.OVERALLWEEKS) {
        if (newWeekCount == i) {
          weekState.append("1")
        } else {
          weekState.append("0")
        }
        i += 1
      }
    } else {
      var i = 1
      while (i <= ExamYearWeekTimeUtil.OVERALLWEEKS) {
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

  def getWeekDayByDate(date: Date): java.lang.Integer = WeekDays.of(date).index

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
      weekday: WeekDay, 
      times: Array[Int]): Date = {
    val weekStates = Strings.repeat("0", ExamYearWeekTimeUtil.OVERALLWEEKS).toCharArray()
    weekStates(teachWeek) = '1'
    val weekState = new String(weekStates)
    val yearWeekState = WeekStates.build(semester, weekState, weekday).head
    val gc = new GregorianCalendar()
    gc.set(Calendar.YEAR, yearWeekState._1)
    gc.set(Calendar.WEEK_OF_YEAR, yearWeekState._2.indexOf("1") + 1)
    gc.set(Calendar.DAY_OF_WEEK, weekday.index)
    gc.set(Calendar.HOUR_OF_DAY, times(0))
    gc.set(Calendar.MINUTE, times(1))
    gc.set(Calendar.MILLISECOND, 0)
    gc.getTime
  }

  def getTeachWeekOfYear(semester: Semester, nowDate: Date): Int = {
    val start = Calendar.getInstance
    start.setTime(semester.beginOn)
    start.setFirstDayOfWeek(semester.firstWeekday.index)
    start.set(Calendar.DAY_OF_WEEK, semester.firstWeekday.index)
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
    val fromYear = fromDate.getYear + 1900
    val nowYear = nowDate.getYear + 1900
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
