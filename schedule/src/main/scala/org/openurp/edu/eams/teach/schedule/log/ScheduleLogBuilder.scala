package org.openurp.edu.eams.teach.schedule.log

import java.util.HashMap
import java.util.Iterator
import java.util.Map
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor

import scala.collection.JavaConversions._

object ScheduleLogBuilder {

  val TYPE = "TYPE"

  val LESSON_ID = "LESSON.ID"

  val LESSON_PROJECT = "LESSON.PROJECT"

  val LESSON_SEMESTER = "LESSON.SEMESTER"

  val LESSON_NO = "LESSON.NO"

  val REASON = "REASON"

  val COURSE_CODE = "LESSON.COURSE.CODE"

  val COURSE_NAME = "LESSON.COURSE.NAME"

  val DETAIL = "DETAIL"

  val LOG_FIELDS = Array(LESSON_ID, TYPE, LESSON_PROJECT, LESSON_SEMESTER, LESSON_NO, COURSE_CODE, COURSE_NAME, REASON, DETAIL)

  object Operation extends Enumeration {

    val CREATE = new Operation()

    val DELETE = new Operation()

    val UPDATE = new Operation()

    class Operation extends Val

    implicit def convertValue(v: Value): Operation = v.asInstanceOf[Operation]
  }

  private def makeInformations(lesson: Lesson): Map[String, String] = {
    val empty = new HashMap[String, String]()
    for (i <- 0 until LOG_FIELDS.length) {
      empty.put(LOG_FIELDS(i), "")
    }
    empty.put(LESSON_ID, String.valueOf(lesson.getId))
    empty.put(LESSON_PROJECT, String.valueOf(lesson.getProject.getName))
    empty.put(LESSON_SEMESTER, lesson.getSemester.getSchoolYear + "学年" + lesson.getSemester.getName + 
      "学期")
    empty.put(LESSON_NO, lesson.getNo)
    empty.put(COURSE_CODE, lesson.getCourse.getCode)
    empty.put(COURSE_NAME, lesson.getCourse.getName)
    empty
  }

  private def toString(informations: Map[String, String]): String = {
    val sb = new StringBuilder()
    for (i <- 0 until LOG_FIELDS.length) {
      sb.append(LOG_FIELDS(i)).append("=").append(informations.get(LOG_FIELDS(i)))
      if (i != LOG_FIELDS.length - 1) {
        sb.append("\n")
      }
    }
    sb.toString
  }

  def create(lesson: Lesson, reason: String): Array[String] = {
    val map = makeInformations(lesson)
    map.put(TYPE, ScheduleLogBuilder.Operation.CREATE.name())
    map.put(DETAIL, stringify(lesson))
    if (Strings.isNotEmpty(reason)) {
      map.put(REASON, reason)
    }
    Array(toString(map), Operation.CREATE.name())
  }

  def delete(lesson: Lesson, reason: String): Array[String] = {
    val map = makeInformations(lesson)
    map.put(TYPE, ScheduleLogBuilder.Operation.DELETE.name())
    if (Strings.isNotEmpty(reason)) {
      map.put(REASON, reason)
    }
    Array(toString(map), Operation.DELETE.name())
  }

  def update(lesson: Lesson, reason: String): Array[String] = {
    val map = makeInformations(lesson)
    map.put(TYPE, ScheduleLogBuilder.Operation.UPDATE.name())
    map.put(DETAIL, stringify(lesson))
    if (Strings.isNotEmpty(reason)) {
      map.put(REASON, reason)
    }
    Array(toString(map), Operation.UPDATE.name())
  }

  private def stringify(lesson: Lesson): String = {
    val sb = new StringBuilder()
    append(sb, "课程序号", lesson.getNo)
    append(sb, "学期", lesson.getSemester.getSchoolYear + "学年" + lesson.getSemester.getName + 
      "学期")
    append(sb, "课程", lesson.getCourse.getName + '[' + lesson.getCourse.getCode + 
      ']')
    append(sb, "教学项目", lesson.getProject.getName)
    append(sb, "教学班", lesson.getTeachClass.getName)
    val tsb = new StringBuilder()
    var iter = lesson.getTeachers.iterator()
    while (iter.hasNext) {
      val teacher = iter.next()
      tsb.append(teacher.getName + '[' + teacher.getCode + ']')
      if (iter.hasNext) {
        tsb.append(',')
      }
    }
    append(sb, "授课教师", tsb.toString)
    append(sb, "课程类别", lesson.getCourseType.getName)
    append(sb, "开课院系", lesson.getTeachDepart.getName)
    append(sb, "年级", lesson.getTeachClass.grade)
    append(sb, "起迄周", lesson.getCourseSchedule.getStartWeek + "-" + lesson.getCourseSchedule.getEndWeek + 
      "周")
    append(sb, "实际人数", lesson.getTeachClass.getStdCount)
    append(sb, "人数上限", lesson.getTeachClass.getLimitCount)
    append(sb, "课时", lesson.getCourseSchedule.getPeriod)
    append(sb, "排课结果", CourseActivityDigestor.getInstance.setDelimeter(";")
      .digest(null, lesson))
    sb.replace(sb.length - 1, sb.length, "")
    sb.toString
  }

  private def append(sb: StringBuilder, fieldName: String, value: AnyRef) {
    if (value == null) {
      sb.append(fieldName).append("=空\n")
    } else {
      sb.append(fieldName).append('=').append(value.toString)
        .append('\n')
    }
  }
}
