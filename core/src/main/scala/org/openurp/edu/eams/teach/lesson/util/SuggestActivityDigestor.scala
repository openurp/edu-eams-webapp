package org.openurp.edu.eams.teach.lesson.util

import java.text.SimpleDateFormat


import org.beangle.commons.bean.comparators.MultiPropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.BitStrings
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.openurp.base.Room
import org.openurp.base.Semester
import org.beangle.commons.lang.time.WeekDays
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.util.TimeUtils
import SuggestActivityDigestor._



object SuggestActivityDigestor {

  val singleTeacher = ":teacher1"

  val multiTeacher = ":teacher+"

  val moreThan1Teacher = ":teacher2"

  val day = ":day"

  val units = ":units"

  val weeks = ":weeks"

  val time = ":time"

  val room = ":room"

  val starton = ":starton"

  val defaultFormat = ":teacher+ :day :units :weeks :room :roomCode"

  def getInstance(): SuggestActivityDigestor = new SuggestActivityDigestor()
}

class SuggestActivityDigestor private () {

  private var delimeter: String = ","

  def digest(textResource: TextResource, arrangeSuggest: ArrangeSuggest): String = {
    digest(textResource, arrangeSuggest, defaultFormat)
  }

  def digest(textResource: TextResource, arrangeSuggest: ArrangeSuggest, format: String): String = {
    val semester = arrangeSuggest.lesson.semester
    val activities = arrangeSuggest.activities
    if (CollectUtils.isEmpty(activities)) return ""
    if (Strings.isEmpty(format)) format = defaultFormat
    val mergedActivities = CollectUtils.newArrayList()
    val teachers = CollectUtils.newHashSet()
    val hasRoom = Strings.contains(format, room)
    val hasTeacher = Strings.contains(format, "teacher")
    val activitiesList = CollectUtils.newArrayList(activities)
    Collections.sort(activitiesList)
    for (activity <- activitiesList) {
      if (hasTeacher) {
        if (CollectUtils.isNotEmpty(activity.teachers)) teachers.addAll(activity.teachers)
      }
      var merged = false
      for (added <- mergedActivities if added.isSameActivityExcept(activity, hasTeacher)) {
        if (added.getTime.startUnit > activity.getTime.startUnit) {
          added.getTime.startUnit=activity.getTime.startUnit
        }
        if (added.getTime.endUnit < activity.getTime.endUnit) {
          added.getTime.endUnit=activity.getTime.endUnit
        }
        added.getTime.newWeekState(BitStrings.or(added.getTime.weekState, activity.getTime.weekState))
        merged = true
      }
      if (!merged) {
        mergedActivities.add(activity)
      }
    }
    var addTeacher = false
    if (hasTeacher) {
      addTeacher = true
      if (format.indexOf(singleTeacher) != -1 && teachers.size != 1) {
        addTeacher = false
      }
      if (format.indexOf(moreThan1Teacher) != -1 && teachers.size < 2) {
        addTeacher = false
      }
      if (format.indexOf(multiTeacher) != -1 && teachers.size == 0) {
        addTeacher = false
      }
    }
    val CourseArrangeBuf = new StringBuffer()
    Collections.sort(mergedActivities, new MultiPropertyComparator("time.day"))
    for (activity <- mergedActivities) {
      CourseArrangeBuf.append(format)
      var replaceStart = 0
      replaceStart = CourseArrangeBuf.indexOf(":teacher")
      if (addTeacher) {
        val teacherStr = new StringBuilder("")
        for (teacher <- activity.teachers) {
          teacherStr.append(teacher.name)
        }
        CourseArrangeBuf.replace(replaceStart, replaceStart + 9, teacherStr.toString)
      } else if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + 9, "")
      }
      replaceStart = CourseArrangeBuf.indexOf(day)
      if (-1 != replaceStart) {
        if (null != textResource && textResource.locale.language == "en") {
          CourseArrangeBuf.replace(replaceStart, replaceStart + day.length, (WeekDays.get(activity.getTime.day)).engName + 
            ".")
        } else {
          CourseArrangeBuf.replace(replaceStart, replaceStart + day.length, (WeekDays.get(activity.getTime.day)).name)
        }
      }
      replaceStart = CourseArrangeBuf.indexOf(units)
      if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + units.length, activity.getTime.startUnit + "-" + activity.getTime.endUnit)
      }
      replaceStart = CourseArrangeBuf.indexOf(time)
      if (-1 != replaceStart) {
        if (0 != activity.getTime.start) {
          CourseArrangeBuf.replace(replaceStart, replaceStart + time.length, TimeUtils.getTimeStr(activity.getTime.start) + 
            "-" + 
            TimeUtils.getTimeStr(activity.getTime.end))
        } else {
          CourseArrangeBuf.replace(replaceStart, replaceStart + time.length, TimeUtils.getTimeStr(activity.getTime.start) + 
            "-" + 
            TimeUtils.getTimeStr(activity.getTime.end))
        }
      }
      replaceStart = CourseArrangeBuf.indexOf(weeks)
      if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + weeks.length, YearWeekTimeUtil.digest(activity.getTime.weekState, 
          CourseTime.FIRST_WEEK_FROM, 1, Semester.OVERALLWEEKS, textResource))
      }
      val sdf = new SimpleDateFormat("M月dd日起")
      replaceStart = CourseArrangeBuf.indexOf(starton)
      if (-1 != replaceStart) {
        val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(semester, activity.getTime)
        if (null != timeUnits && timeUnits.length > 0) {
          val unit = timeUnits(0)
          CourseArrangeBuf.replace(replaceStart, replaceStart + starton.length, sdf.format(unit.firstDay))
        }
      }
      CourseArrangeBuf.append(" ").append(delimeter)
    }
    if (CourseArrangeBuf.lastIndexOf(delimeter) != -1) CourseArrangeBuf.delete(CourseArrangeBuf.lastIndexOf(delimeter), 
      CourseArrangeBuf.length)
    CourseArrangeBuf.toString
  }

  def setDelimeter(delimeter: String): SuggestActivityDigestor = {
    this.delimeter = delimeter
    this
  }
}
