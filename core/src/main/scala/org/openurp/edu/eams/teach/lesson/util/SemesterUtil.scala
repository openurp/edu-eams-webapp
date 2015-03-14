package org.openurp.edu.eams.teach.lesson.util

import java.util.Calendar
import java.util.GregorianCalendar
import org.openurp.base.Semester
import org.openurp.edu.eams.date.EamsWeekday
import org.openurp.edu.eams.date.RelativeDateUtil

import scala.collection.JavaConversions._

object SemesterUtil {

  def getWeekTime(semester: Semester, weekIndex: Int): Array[java.util.Date] = {
    val dates = Array.ofDim[java.util.Date](2)
    val relativeDateUtil = RelativeDateUtil.startOn(semester)
    val startOn = if (semester.getFirstWeekday == Calendar.SUNDAY) EamsWeekday.SUNDAY else EamsWeekday.MONDAY
    dates(0) = relativeDateUtil.getDate(weekIndex, startOn)
    dates(1) = relativeDateUtil.getDate(weekIndex + 1, startOn)
    dates
  }

  def getStartYear(semester: Semester): Int = {
    if (null != semester.beginOn) {
      val gc = new GregorianCalendar()
      gc.setTime(semester.beginOn)
      return gc.get(Calendar.YEAR)
    }
    0
  }
}
