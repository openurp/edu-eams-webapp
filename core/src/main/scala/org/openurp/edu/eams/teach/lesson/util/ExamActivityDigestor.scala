package org.openurp.edu.eams.teach.lesson.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar

import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.eams.date.EamsDateUtil
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.exam.ExamRoom
import ExamActivityDigestor._

object ExamActivityDigestor {

  val singleTeacher = ":teacher1"

  val multiTeacher = ":teacher+"

  val moreThan1Teacher = ":teacher2"

  val day = ":day"

  val date = ":date"

  val units = ":units"

  val weeks = ":weeks"

  val time = ":time"

  val room = ":room"

  val building = ":building"

  val district = ":district"

  val defaultFormat = ":date :time 第:weeks周 :day"

  val df = new SimpleDateFormat("yyyy-MM-dd")

  def getInstance(): ExamActivityDigestor = new ExamActivityDigestor()
}

class ExamActivityDigestor private () {

  private var delimeter: String = ","

  def digest(activity: ExamActivity, resource: TextResource): String = {
    digest(activity, resource, defaultFormat)
  }

  def digest(activity: ExamActivity, resource: TextResource, format: String): String = {
    if (null == activity) return ""
    if (Strings.isEmpty(format)) {
      format = defaultFormat
    }
    val hasRoom = Strings.contains(format, room)
    val hasTeacher = Strings.contains(format, "teacher")
    val arrangeInfoBuf = new StringBuffer()
    var iter = activity.examRooms.iterator
    while (iter.hasNext) {
      val examRoom = iter.next()
      arrangeInfoBuf.append(format)
      var replaceStart = 0
      replaceStart = arrangeInfoBuf.indexOf(day)
      if (-1 != replaceStart) {
        var dayIndex = activity.startAt.day
        if (dayIndex == 0) {
          dayIndex = 6
        } else {
          dayIndex -= 1
        }
        arrangeInfoBuf.replace(replaceStart, replaceStart + day.length, WeekDays.All(dayIndex).name)
      }
      replaceStart = arrangeInfoBuf.indexOf(units)
      if (-1 != replaceStart) {
      }
      replaceStart = arrangeInfoBuf.indexOf(time)
      if (-1 != replaceStart) {
        val sdm = new SimpleDateFormat("HH:mm")
        arrangeInfoBuf.replace(replaceStart, replaceStart + time.length, sdm.format(activity.startAt) + "-" + sdm.format(activity.endAt))
      }
      replaceStart = arrangeInfoBuf.indexOf(date)
      if (-1 != replaceStart) {
        arrangeInfoBuf.replace(replaceStart, replaceStart + date.length, df.format(activity.startAt))
      }
      replaceStart = arrangeInfoBuf.indexOf(weeks)
      if (-1 != replaceStart) {
        val teachWeek = EamsDateUtil.SUNDAY_FIRST.weekOfYear(examRoom.getsemester.beginOn)
        val examWeek = EamsDateUtil.SUNDAY_FIRST.weekOfYear(activity.startAt)
        val c = Calendar.getInstance
        c.setTime(activity.startAt)
        if (c.get(Calendar.YEAR) > activity.semester.startYear) {
          val year = SemesterUtil.startYear(activity.semester)
          val LastDay = year + "-12-31"
          val gregorianCalendar = new GregorianCalendar()
          gregorianCalendar.setTime(java.sql.Date.valueOf(LastDay))
          var endAtSat = false
          if (gregorianCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            endAtSat = true
          }
          var yearWeeks = 53
          if (!endAtSat) {
            yearWeeks += 1
          }
          arrangeInfoBuf.replace(replaceStart, replaceStart + weeks.length, yearWeeks - teachWeek + examWeek + "")
        } else {
          arrangeInfoBuf.replace(replaceStart, replaceStart + weeks.length, examWeek - teachWeek + 1 + "")
        }
      }
      replaceStart = arrangeInfoBuf.indexOf(room)
      if (-1 != replaceStart) {
        arrangeInfoBuf.replace(replaceStart, replaceStart + room.length, if ((null != examRoom.room)) examRoom.room.name else "")
        replaceStart = arrangeInfoBuf.indexOf(building)
        if (-1 != replaceStart) {
          if (null != examRoom.room && null != examRoom.room.building) {
            arrangeInfoBuf.replace(replaceStart, replaceStart + building.length, examRoom.room.building.name)
          } else {
            arrangeInfoBuf.replace(replaceStart, replaceStart + building.length, "")
          }
        }
        replaceStart = arrangeInfoBuf.indexOf(district)
        if (-1 != replaceStart) {
          if (null != examRoom.room && null != examRoom.room.building && 
            null != examRoom.room.building.campus) {
            arrangeInfoBuf.replace(replaceStart, replaceStart + district.length, examRoom.room.building.campus.name)
          } else {
            arrangeInfoBuf.replace(replaceStart, replaceStart + district.length, "")
          }
        }
      }
      arrangeInfoBuf.append(delimeter)
    }
    if (arrangeInfoBuf.lastIndexOf(delimeter) != -1) arrangeInfoBuf.delete(arrangeInfoBuf.lastIndexOf(delimeter), 
      arrangeInfoBuf.length)
    arrangeInfoBuf.toString
  }

  def setDelimeter(delimeter: String): ExamActivityDigestor = {
    this.delimeter = delimeter
    this
  }
}
