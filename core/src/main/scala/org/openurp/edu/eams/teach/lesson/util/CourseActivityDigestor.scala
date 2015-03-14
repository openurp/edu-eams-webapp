package org.openurp.edu.eams.teach.lesson.util

import java.text.SimpleDateFormat
import java.util.Collection
import java.util.Collections
import java.util.Iterator
import java.util.List
import java.util.Set
import org.beangle.commons.bean.comparators.MultiPropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.BitStrings
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.openurp.edu.eams.base.Classroom
import org.openurp.base.Semester
import org.openurp.edu.eams.base.util.WeekDays
import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.number.NumberRangeDigestor
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.util.TimeUtils
import org.openurp.edu.eams.weekstate.SemesterWeekStateBuilder
import org.openurp.edu.eams.weekstate.WeekStateDirection
import CourseActivityDigestor._

import scala.collection.JavaConversions._

object CourseActivityDigestor {

  val singleTeacher = ":teacher1"

  val multiTeacher = ":teacher+"

  val moreThan1Teacher = ":teacher2"

  val day = ":day"

  val units = ":units"

  val weeks = ":weeks"

  val time = ":time"

  val room = ":room"

  val roomCode = ":roomCode"

  val building = ":building"

  val district = ":district"

  val lesson = ":lesson"

  val course = ":course"

  val starton = ":starton"

  val defaultFormat = ":teacher+ :day :units :weeks :room :roomCode"

  def getInstance(): CourseActivityDigestor = new CourseActivityDigestor()
}

class CourseActivityDigestor private () {

  private var delimeter: String = ","

  def digest(textResource: TextResource, lesson: Lesson): String = {
    digest(textResource, lesson, defaultFormat)
  }

  def digest(textResource: TextResource, lesson: Lesson, format: String): String = {
    digest(textResource, lesson.getCourseSchedule.getActivities, format)
  }

  def digest(textResource: TextResource, activities: Collection[CourseActivity]): String = {
    digest(textResource, activities, defaultFormat)
  }

  def digest(textResource: TextResource, activities: Collection[CourseActivity], format: String): String = {
    if (CollectUtils.isEmpty(activities)) return ""
    if (Strings.isEmpty(format)) format = defaultFormat
    val semester = activities.iterator().next().getLesson.getSemester
    val mergedActivities = CollectUtils.newArrayList()
    val teachers = CollectUtils.newHashSet()
    val hasRoom = Strings.contains(format, room)
    val hasTeacher = Strings.contains(format, "teacher")
    val activitiesList = CollectUtils.newArrayList(activities)
    Collections.sort(activitiesList)
    for (activity <- activitiesList) {
      if (hasTeacher) {
        if (CollectUtils.isNotEmpty(activity.getTeachers)) teachers.addAll(activity.getTeachers)
      }
      var merged = false
      for (added <- mergedActivities if added.isSameActivityExcept(activity, hasTeacher, hasRoom)) {
        if (added.getTime.getStartUnit > activity.getTime.getStartUnit) {
          added.getTime.setStartUnit(activity.getTime.getStartUnit)
        }
        if (added.getTime.getEndUnit < activity.getTime.getEndUnit) {
          added.getTime.setEndUnit(activity.getTime.getEndUnit)
        }
        added.getTime.newWeekState(BitStrings.or(added.getTime.getWeekState, activity.getTime.getWeekState))
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
    Collections.sort(mergedActivities, new MultiPropertyComparator("lesson.course.code,time.weekday"))
    for (activity <- mergedActivities) {
      CourseArrangeBuf.append(format)
      var replaceStart = 0
      replaceStart = CourseArrangeBuf.indexOf(":teacher")
      if (addTeacher) {
        val teacherStr = new StringBuilder("")
        for (teacher <- activity.getTeachers) {
          teacherStr.append(teacher.getName)
        }
        CourseArrangeBuf.replace(replaceStart, replaceStart + 9, teacherStr.toString)
      } else if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + 9, "")
      }
      replaceStart = CourseArrangeBuf.indexOf(day)
      if (-1 != replaceStart) {
        if (null != textResource && textResource.getLocale.getLanguage == "en") {
          CourseArrangeBuf.replace(replaceStart, replaceStart + day.length, (WeekDays.get(activity.getTime.getWeekday)).getEngName + 
            ".")
        } else {
          CourseArrangeBuf.replace(replaceStart, replaceStart + day.length, (WeekDays.get(activity.getTime.getWeekday)).getName)
        }
      }
      replaceStart = CourseArrangeBuf.indexOf(units)
      if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + units.length, activity.getTime.getStartUnit + "-" + activity.getTime.getEndUnit)
      }
      replaceStart = CourseArrangeBuf.indexOf(time)
      if (-1 != replaceStart) {
        if (0 != activity.getTime.getStartTime) {
          CourseArrangeBuf.replace(replaceStart, replaceStart + time.length, TimeUtils.getTimeStr(activity.getTime.getStartTime) + 
            "-" + 
            TimeUtils.getTimeStr(activity.getTime.getEndTime))
        } else {
          CourseArrangeBuf.replace(replaceStart, replaceStart + time.length, TimeUtils.getTimeStr(activity.getTime.getStartTime) + 
            "-" + 
            TimeUtils.getTimeStr(activity.getTime.getEndTime))
        }
      }
      replaceStart = CourseArrangeBuf.indexOf(lesson)
      if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + lesson.length, activity.getLesson.getNo)
      }
      replaceStart = CourseArrangeBuf.indexOf(course)
      if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + course.length, activity.getLesson.getCourse.getName + "(" + activity.getLesson.getCourse.getCode + 
          ")")
      }
      replaceStart = CourseArrangeBuf.indexOf(weeks)
      if (-1 != replaceStart) {
        val weekIndeciesInSemester = SemesterWeekStateBuilder.parse(activity.getTime.getWeekState, WeekStateDirection.LTR)
        CourseArrangeBuf.replace(replaceStart, replaceStart + weeks.length, NumberRangeDigestor.digest(weekIndeciesInSemester, 
          textResource) + 
          " ")
      }
      val sdf = new SimpleDateFormat("M月dd日起")
      replaceStart = CourseArrangeBuf.indexOf(starton)
      if (-1 != replaceStart) {
        val timeUnits = TimeUnitUtil.convertToTimeUnits(semester, activity.getTime)
        if (null != timeUnits && timeUnits.length > 0) {
          val unit = timeUnits(0)
          CourseArrangeBuf.replace(replaceStart, replaceStart + starton.length, sdf.format(unit.getFirstDay))
        }
      }
      replaceStart = CourseArrangeBuf.indexOf(room)
      if (-1 != replaceStart) {
        val rooms = activity.getRooms
        val roomStr = new StringBuilder("")
        var it = rooms.iterator()
        while (it.hasNext) {
          val room = it.next()
          roomStr.append(room.getName)
          if (it.hasNext) {
            roomStr.append(",")
          }
        }
        CourseArrangeBuf.replace(replaceStart, replaceStart + room.length, roomStr.toString)
        replaceStart = CourseArrangeBuf.indexOf(building)
        if (-1 != replaceStart) {
          val buildingStr = new StringBuilder("")
          var iterator = rooms.iterator()
          while (iterator.hasNext) {
            val room = iterator.next()
            buildingStr.append(room.getBuilding.getName)
            if (iterator.hasNext) {
              buildingStr.append(",")
            }
          }
          CourseArrangeBuf.replace(replaceStart, replaceStart + building.length, buildingStr.toString)
        }
        replaceStart = CourseArrangeBuf.indexOf(roomCode)
        if (-1 != replaceStart) {
          val roomCodeStr = new StringBuilder("")
          var iterator = rooms.iterator()
          while (iterator.hasNext) {
            val room = iterator.next()
            roomCodeStr.append(room.getCode)
            if (iterator.hasNext) {
              roomCodeStr.append(",")
            }
          }
          CourseArrangeBuf.replace(replaceStart, replaceStart + roomCode.length, roomCodeStr.toString)
        }
        replaceStart = CourseArrangeBuf.indexOf(district)
        if (-1 != replaceStart) {
          val districtStr = new StringBuilder("")
          var it = rooms.iterator()
          while (it.hasNext) {
            val room = it.next()
            districtStr.append(room.getCampus.getName)
            if (it.hasNext) {
              districtStr.append(",")
            }
          }
          CourseArrangeBuf.replace(replaceStart, replaceStart + district.length, districtStr.toString)
        }
      }
      CourseArrangeBuf.append(" ").append(delimeter)
    }
    if (CourseArrangeBuf.lastIndexOf(delimeter) != -1) CourseArrangeBuf.delete(CourseArrangeBuf.lastIndexOf(delimeter), 
      CourseArrangeBuf.length)
    CourseArrangeBuf.toString
  }

  def setDelimeter(delimeter: String): CourseActivityDigestor = {
    this.delimeter = delimeter
    this
  }
}
