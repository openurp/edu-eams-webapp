package org.openurp.edu.eams.date

import java.util.Arrays
import java.util.Calendar

object EamsWeekday extends Enumeration {

  val MONDAY = new EamsWeekday(1, "星期一", "Mon", "weekday.mon")

  val TUESDAY = new EamsWeekday(2, "星期二", "Tue", "weekday.tue")

  val WEDNESDAY = new EamsWeekday(3, "星期三", "Wed", "weekday.wed")

  val THURSDAY = new EamsWeekday(4, "星期四", "Thur", "weekday.thur")

  val FRIDAY = new EamsWeekday(5, "星期五", "Fri", "weekday.fri")

  val SATURDAY = new EamsWeekday(6, "星期六", "Sat", "weekday.sat")

  val SUNDAY = new EamsWeekday(7, "星期日", "Sun", "weekday.sun")

  class EamsWeekday(val index: Int,
    val name: String,
    val engName: String,
    val i18nKey: String) extends Val {

    def getId(): java.lang.Integer = this.index

    def getJdkIndex(): Int = this match {
      case MONDAY => Calendar.MONDAY
      case TUESDAY => Calendar.TUESDAY
      case WEDNESDAY => Calendar.WEDNESDAY
      case THURSDAY => Calendar.THURSDAY
      case FRIDAY => Calendar.FRIDAY
      case SATURDAY => Calendar.SATURDAY
      case SUNDAY => Calendar.SUNDAY
    }
  }

  def getDay(eamsWeekdayIndex: Int): EamsWeekday = eamsWeekdayIndex match {
    case 1 => MONDAY
    case 2 => TUESDAY
    case 3 => WEDNESDAY
    case 4 => THURSDAY
    case 5 => FRIDAY
    case 6 => SATURDAY
    case 7 => SUNDAY
  }

  def getDayByJdkIndex(jdkWeekdayIndex: Int): EamsWeekday = jdkWeekdayIndex match {
    case Calendar.MONDAY => MONDAY
    case Calendar.TUESDAY => TUESDAY
    case Calendar.WEDNESDAY => WEDNESDAY
    case Calendar.THURSDAY => THURSDAY
    case Calendar.FRIDAY => FRIDAY
    case Calendar.SATURDAY => SATURDAY
    case Calendar.SUNDAY => SUNDAY
  }

  def getWeekdayList(firstDayOnSunday: Boolean): List[EamsWeekday] = {
    getWeekdayArray(firstDayOnSunday).toList
  }

  def getWeekdayArray(firstDayOnSunday: Boolean): Array[EamsWeekday] = {
    if (firstDayOnSunday) {
      val res = Array.ofDim[EamsWeekday](7)
      for (weekday <- EamsWeekday.values) {
        res(weekday.getJdkIndex - 1) = weekday
      }
      return res
    }
    EamsWeekday.values.toArray.asInstanceOf[Array[EamsWeekday]]
  }

  implicit def convertValue(v: Value): EamsWeekday = v.asInstanceOf[EamsWeekday]
}
