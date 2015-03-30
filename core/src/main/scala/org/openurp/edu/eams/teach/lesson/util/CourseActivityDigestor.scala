package org.openurp.edu.eams.teach.lesson.util

import java.text.SimpleDateFormat

import org.beangle.commons.bean.orderings.MultiPropertyOrdering
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.number.NumberRangeDigestor
import org.openurp.edu.eams.weekstate.WeekStates
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.schedule.CourseActivity

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

class CourseActivityDigestor {

  import CourseActivityDigestor._
  private var delimeter: String = ","

  def digest(textResource: TextResource, lesson: Lesson): String = {
    digest(textResource, lesson, defaultFormat)
  }

  def digest(textResource: TextResource, lesson: Lesson, format: String): String = {
    digest(textResource, lesson.schedule.activities, format)
  }

  def digest(textResource: TextResource, activities: Iterable[CourseActivity]): String = {
    digest(textResource, activities, defaultFormat)
  }

  private def isSameActivityExcept(activity1: CourseActivity, activity2: CourseActivity, hasTeacher: Boolean, hasRoom: Boolean): Boolean = {
    //FIXME
    false
  }
  def digest(textResource: TextResource, activities: Iterable[CourseActivity], f: String): String = {
    if (Collections.isEmpty(activities)) return ""
    val format = if (Strings.isEmpty(f)) defaultFormat else f
    val semester = activities.iterator.next().lesson.semester
    val mergedActivities = Collections.newBuffer[CourseActivity]
    val teachers = Collections.newSet[Teacher]
    val hasRoom = Strings.contains(format, room)
    val hasTeacher = Strings.contains(format, "teacher")
    val activitiesList = Collections.newBuffer[CourseActivity]
    activitiesList ++= (activities)
    activitiesList.sorted
    for (activity <- activitiesList) {
      if (hasTeacher) {
        if (Collections.isNotEmpty(activity.teachers)) teachers ++= activity.teachers
      }
      var merged = false
      for (added <- mergedActivities if isSameActivityExcept(added, activity, hasTeacher, hasRoom)) {
        if (added.time.begin.value > activity.time.begin.value) {
          added.time.begin = activity.time.begin
        }
        if (added.time.end.value < activity.time.end.value) {
          added.time.end = activity.time.end
        }
        added.time.state = added.time.state | activity.time.state
        merged = true
      }
      if (!merged) {
        mergedActivities += (activity)
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
    mergedActivities.sorted(new MultiPropertyOrdering("lesson.course.code,time.day"))
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
        if (null != textResource && textResource.locale.getLanguage == "en") {
          CourseArrangeBuf.replace(replaceStart, replaceStart + day.length, activity.time.day.toString)
        } else {
          CourseArrangeBuf.replace(replaceStart, replaceStart + day.length, activity.time.day.toString)
        }
      }
      replaceStart = CourseArrangeBuf.indexOf(units)
      if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + units.length, activity.time.begin + "-" + activity.time.end)
      }
      replaceStart = CourseArrangeBuf.indexOf(time)
      if (-1 != replaceStart) {
        if (null != activity.time.begin) {
          CourseArrangeBuf.replace(replaceStart, replaceStart + time.length, activity.time.begin.toString +
            "-" + activity.time.end.toString)
        } else {
          CourseArrangeBuf.replace(replaceStart, replaceStart + time.length, activity.time.begin.toString +
            "-" + activity.time.end.toString)
        }
      }
      replaceStart = CourseArrangeBuf.indexOf(lesson)
      if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + lesson.length, activity.lesson.no)
      }
      replaceStart = CourseArrangeBuf.indexOf(course)
      if (-1 != replaceStart) {
        CourseArrangeBuf.replace(replaceStart, replaceStart + course.length, activity.lesson.course.name + "(" + activity.lesson.course.code +
          ")")
      }
      replaceStart = CourseArrangeBuf.indexOf(weeks)
      if (-1 != replaceStart) {
        val weekIndeciesInSemester = WeekStates.parse(activity.time.state.toString)
        CourseArrangeBuf.replace(replaceStart, replaceStart + weeks.length, NumberRangeDigestor.digest(weekIndeciesInSemester,
          textResource) +
          " ")
      }
      val sdf = new SimpleDateFormat("M月dd日起")
      replaceStart = CourseArrangeBuf.indexOf(starton)
      if (-1 != replaceStart) {
        val unit = activity.time
        CourseArrangeBuf.replace(replaceStart, replaceStart + starton.length, sdf.format(unit.firstDate))
      }
      replaceStart = CourseArrangeBuf.indexOf(room)
      if (-1 != replaceStart) {
        val rooms = activity.rooms
        val roomStr = new StringBuilder("")
        var it = rooms.iterator
        while (it.hasNext) {
          val room = it.next()
          roomStr.append(room.name)
          if (it.hasNext) {
            roomStr.append(",")
          }
        }
        CourseArrangeBuf.replace(replaceStart, replaceStart + room.length, roomStr.toString)
        replaceStart = CourseArrangeBuf.indexOf(building)
        if (-1 != replaceStart) {
          val buildingStr = new StringBuilder("")
          var iterator = rooms.iterator
          while (iterator.hasNext) {
            val room = iterator.next()
            buildingStr.append(room.building.name)
            if (iterator.hasNext) {
              buildingStr.append(",")
            }
          }
          CourseArrangeBuf.replace(replaceStart, replaceStart + building.length, buildingStr.toString)
        }
        replaceStart = CourseArrangeBuf.indexOf(roomCode)
        if (-1 != replaceStart) {
          val roomCodeStr = new StringBuilder("")
          var iterator = rooms.iterator
          while (iterator.hasNext) {
            val room = iterator.next()
            roomCodeStr.append(room.code)
            if (iterator.hasNext) {
              roomCodeStr.append(",")
            }
          }
          CourseArrangeBuf.replace(replaceStart, replaceStart + roomCode.length, roomCodeStr.toString)
        }
        replaceStart = CourseArrangeBuf.indexOf(district)
        if (-1 != replaceStart) {
          val districtStr = new StringBuilder("")
          var it = rooms.iterator
          while (it.hasNext) {
            val room = it.next()
            districtStr.append(room.campus.name)
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
