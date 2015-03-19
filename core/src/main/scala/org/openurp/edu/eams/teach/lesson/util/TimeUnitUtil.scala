package org.openurp.edu.eams.teach.lesson.util

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar

import java.util.Comparator
import java.util.Date
import java.util.GregorianCalendar

import java.util.Vector
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.openurp.base.Semester
import org.openurp.base.model.SemesterBean
import org.openurp.edu.eams.base.util.WeekUnit
import 
import org.openurp.edu.eams.date.EamsDateUtil
import org.openurp.edu.eams.number.NumberSequence
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.weekstate.SemesterWeekTime
import org.openurp.edu.eams.weekstate.SemesterWeekTimeBuilder



object YearWeekTimeUtil {

  def digests(weekOccupyStr: String, 
      from: Int, 
      startWeek: Int, 
      endWeek: Int): Vector[WeekUnit] = {
    if (null == weekOccupyStr || weekOccupyStr.indexOf('1') == -1) {
      return null
    }
    val occupyWeeks = new Vector[WeekUnit]()
    val initLength = weekOccupyStr.length
    val weekOccupy = new StringBuffer()
    if (from > 1) {
      val before = weekOccupyStr.substring(0, from - 1)
      weekOccupyStr = weekOccupyStr + before
    }
    var repeat = from + startWeek - 2
    if (repeat < 0) {
      repeat = 0
    }
    weekOccupy.append(Strings.repeat("0", repeat))
    weekOccupy.append(weekOccupyStr.substring(repeat, from + endWeek - 1))
    weekOccupy.append(Strings.repeat("0", initLength - weekOccupy.length))
    weekOccupy.append("000")
    if (weekOccupy.indexOf("1") == -1) {
      return occupyWeeks
    }
    var start = 0
    while ('1' != weekOccupy.charAt(start)) {
      start += 1
    }
    var i = start + 1
    while (i < weekOccupy.length) {
      val post = weekOccupy.charAt(start + 1)
      if (post == '0') {
        start = digestOdd(occupyWeeks, weekOccupy, from, start)
      }
      if (post == '1') {
        start = digestContinue(occupyWeeks, weekOccupy, from, start)
      }
      while (start < weekOccupy.length && '1' != weekOccupy.charAt(start)) {
        start += 1
      }
      i = start
    }
    occupyWeeks
  }

  private def digestOdd(occupyWeeks: Vector[WeekUnit], 
      weekOccupy: StringBuffer, 
      from: Int, 
      start: Int): Int = {
    var cycle = 0
    cycle = if ((start - from + 2) % 2 == 0) 3 else 2
    var i = start + 2
    while (i < weekOccupy.length) {
      if (weekOccupy.charAt(i) == '1') {
        if (weekOccupy.charAt(i + 1) == '1') {
          occupyWeeks.add(new WeekUnit(cycle, start - from + 2, i - 2 - from + 2))
          return i
        }
      } else {
        if (i - 2 == start) cycle = 1
        occupyWeeks.add(new WeekUnit(cycle, start - from + 2, i - 2 - from + 2))
        return i + 1
      }
      i += 2
    }
    i
  }

  private def digestContinue(occupyWeeks: Vector[WeekUnit], 
      weekOccupy: StringBuffer, 
      from: Int, 
      start: Int): Int = {
    val cycle = 1
    var i = start + 2
    while (i < weekOccupy.length) {
      if (weekOccupy.charAt(i) == '1') {
        if (weekOccupy.charAt(i + 1) != '1') {
          occupyWeeks.add(new WeekUnit(cycle, start - from + 2, i - from + 2))
          return i + 2
        }
      } else {
        occupyWeeks.add(new WeekUnit(cycle, start - from + 2, i - 1 - from + 2))
        return i + 1
      }
      i += 2
    }
    i
  }

  def digest(weekOccupyStr: String, 
      from: Int, 
      startWeek: Int, 
      endWeek: Int, 
      resourses: TextResource, 
      format: String): String = {
    val weekUnitVector = YearWeekTimeUtil.digests(weekOccupyStr, from, startWeek, endWeek)
    var needI18N = false
    val weekRegular = Array("", "", "单", "双", "")
    val weekRegularKeys = Array("", "week.continuely", "week.odd", "week.even", "week.random")
    if (null != resourses) needI18N = true
    if (null != weekUnitVector && !weekUnitVector.isEmpty) {
      val weekUnits = new StringBuffer()
      for (weekUnit <- weekUnitVector) {
        if (weekUnit.getStart == weekUnit.getEnd) {
          weekUnits.append(format.charAt(0)).append(weekUnit.getStart)
            .append(format.charAt(2))
        } else {
          if (needI18N) {
            if (null == weekRegular(weekUnit.getCycle)) {
              weekRegular(weekUnit.getCycle) = resourses.getText(weekRegularKeys(weekUnit.getCycle))
            }
          }
          weekUnits.append(weekRegular(weekUnit.getCycle))
          weekUnits.append(format.charAt(0)).append(weekUnit.getStart)
            .append(format.charAt(1))
            .append(weekUnit.getEnd)
            .append(format.charAt(2))
        }
        weekUnits.append(format.charAt(3))
      }
      if (weekUnits.lastIndexOf(format.charAt(3) + "") == weekUnits.length - 1) {
        weekUnits.substring(0, weekUnits.length - 1)
      } else {
        weekUnits.toString
      }
    } else {
      ""
    }
  }

  def digest(weekOccupyStr: String, 
      from: Int, 
      startWeek: Int, 
      endWeek: Int, 
      resourse: TextResource): String = {
    digest(weekOccupyStr, from, startWeek, endWeek, resourse, "[-] ")
  }

  def convertToYearWeekTimes(semester: Semester, courseTimes: CourseTime*): Array[YearWeekTime] = {
    if (org.beangle.commons.lang.Arrays.isEmpty(courseTimes) || 
      semester == null) {
      return Array.ofDim[YearWeekTime](0)
    }
    val year = SemesterUtil.getStartYear(semester)
    val LastDay = year + "-12-31"
    val gregorianCalendar = new GregorianCalendar()
    gregorianCalendar.setTime(java.sql.Date.valueOf(LastDay))
    var endAtSat = false
    if (gregorianCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
      endAtSat = true
    }
    val unitList = CollectUtils.newArrayList()
    for (courseTime <- courseTimes) {
      val weekState = courseTime.getWeekState
      val sb = new StringBuffer(Strings.repeat("0", semester.getStartWeek - 1))
      sb.append(Strings.substring(weekState, 1, semester.getWeeks + 1))
        .append(Strings.repeat("0", Semester.OVERALLWEEKS * 2 - sb.length))
      if (!endAtSat) {
        sb.insert(Semester.OVERALLWEEKS, "0")
      }
      if (sb.substring(0, Semester.OVERALLWEEKS).indexOf("1") != 
        -1) {
        val unit = new YearWeekTime()
        unit.setYear(year)
        unit.setWeekday(courseTime.day)
        unit.setEndTime(courseTime.end)
        unit.setStartTime(courseTime.start)
        unit.newWeekState(sb.substring(0, Semester.OVERALLWEEKS))
        unitList.add(unit)
      }
      if (sb.substring(Semester.OVERALLWEEKS, 2 * Semester.OVERALLWEEKS)
        .indexOf("1") != 
        -1) {
        val unit = new YearWeekTime()
        unit.setYear(year + 1)
        unit.setWeekday(courseTime.day)
        unit.setStartTime(courseTime.start)
        unit.setEndTime(courseTime.end)
        unit.newWeekState(sb.substring(Semester.OVERALLWEEKS, 2 * Semester.OVERALLWEEKS))
        unitList.add(unit)
      }
    }
    unitList.toArray(Array.ofDim[YearWeekTime](unitList.size))
  }

  def convertToYearWeekTimes(lesson: Lesson, courseTimes: CourseTime*): Array[YearWeekTime] = {
    convertToYearWeekTimes(lesson.getSemester, courseTimes)
  }

  def buildYearWeekTimes(from: Int, 
      startWeek: Int, 
      endWeek: Int, 
      cycle: Int): CourseTime = {
    val sb = new StringBuffer(Strings.repeat("0", from + startWeek - 2))
    var i = startWeek
    while (i <= endWeek) {
      if (isAccording(i, cycle)) {
        sb.append('1')
      } else {
        sb.append('0')
      }
      i += 1
    }
    sb.append(Strings.repeat("0", Semester.OVERALLWEEKS - sb.length))
    val unit = new CourseTime()
    unit.newWeekState(sb.toString)
    unit
  }

  private def isAccording(num: Int, cycle: Int): Boolean = {
    if (cycle == CourseTime.EVEN) num % 2 == 0 else if (cycle == CourseTime.ODD) {
      num % 2 == 1
    } else if (cycle == CourseTime.CONTINUELY) {
      true
    } else {
      false
    }
  }

  def buildFirstLessonDay(lesson: Lesson): Date = {
    val activities = CollectUtils.newArrayList(lesson.getCourseSchedule.getActivities)
    if (activities.size > 1) {
      Collections.sort(activities, new Comparator[CourseActivity]() {

        def compare(activity1: CourseActivity, activity2: CourseActivity): Int = {
          val time1 = activity1.getTime
          val time2 = activity2.getTime
          if (time1.getWeekState.indexOf("1") == time2.getWeekState.indexOf("1")) {
            return time2.day - time1.day
          } else {
            return if (time1.getWeekState.indexOf("1") < time2.getWeekState.indexOf("1")) 1 else -1
          }
        }
      })
    } else if (activities.isEmpty) {
      return null
    }
    val activity = activities.get(0)
    val units = convertToYearWeekTimes(lesson, activity.getTime)
    if (null != units && units.length > 0) {
      val unit = units(0)
      val calendar = new GregorianCalendar()
      calendar.set(Calendar.YEAR, unit.year)
      calendar.set(Calendar.WEEK_OF_YEAR, (unit.state.indexOf("1") + 1) - (CourseTime.FIRST_WEEK_FROM - 1))
      val weekday = if (unit.day == 7) unit.day - 6 else unit.day + 1
      calendar.set(Calendar.DAY_OF_WEEK, weekday)
      return calendar.getTime
    }
    null
  }

  def buildCourseTime(startAt: Date, endAt: Date, semester: Semester): CourseTime = {
    val f = new SimpleDateFormat("HH:mm")
    val time = new CourseTime()
    time.setStartTime(getTimeNumber(f.format(startAt)))
    time.setEndTime(getTimeNumber(f.format(endAt)))
    time.setWeekday(EamsDateUtil.day(startAt).getIndex)
    time.newWeekState(getWeekState(startAt, semester))
    time
  }

  def getWeekOfYear(date: Date): Int = {
    val c = new GregorianCalendar()
    c.setFirstDayOfWeek(Calendar.MONDAY)
    c.setMinimalDaysInFirstWeek(7)
    c.setTime(date)
    c.get(Calendar.WEEK_OF_YEAR)
  }

  def getWeekState(date: Date, semester: Semester): String = {
    val semesterYear = semester.getStartYear
    val gc = new GregorianCalendar()
    gc.setTime(date)
    val activityYear = gc.get(java.util.Calendar.YEAR)
    val w = getWeekOfYear(date)
    val semesterWeek = getWeekOfYear(semester.beginOn)
    val weekState = new StringBuffer()
    if (semesterYear == activityYear) {
      var i = 1
      while (i <= Semester.OVERALLWEEKS) {
        if (i == w) {
          weekState.append("1")
        } else {
          weekState.append("0")
        }
        i += 1
      }
      weekState.substring(semesterWeek - 1) + 
        Strings.repeat("0", Semester.OVERALLWEEKS - weekState.substring(semesterWeek - 1).length)
    } else {
      var i = 1
      while (i <= Semester.OVERALLWEEKS) {
        if (i == w) {
          weekState.append("1")
        } else {
          weekState.append("0")
        }
        i += 1
      }
      val weekStateStr = Strings.repeat("0", Semester.OVERALLWEEKS - semesterWeek)
      weekStateStr + 
        weekState.substring(0, Semester.OVERALLWEEKS - weekStateStr.length)
    }
  }

  def getTimeNumber(time: String): Int = getTimeNumber(time, ":")

  def getTimeNumber(time: String, delimter: String): Int = {
    val index = time.indexOf(delimter)
    java.lang.Integer.parseInt(time.substring(0, index) + time.substring(index + 1, index + 3))
  }

  def main(args: Array[String]) {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val sb = new SemesterBean("2012-2013", "1", new java.sql.Date(format.parse("2012-09-03").getTime), 
      new java.sql.Date(format.parse("2012-2-1").getTime))
    sb.setFirstWeekday(2)
    val t = YearWeekTimeUtil.buildYearWeekTimes(1, 3, 20, 1)
    println(sb.getStartWeek)
    println(t.getWeekState)
    t.setWeekday(1)
    t.setEndTime(1900)
    t.setStartTime(1800)
    println(convertToYearWeekTimes(sb, t)(0))
    println(convertToYearWeekTimes(sb, t)(1))
  }
}
