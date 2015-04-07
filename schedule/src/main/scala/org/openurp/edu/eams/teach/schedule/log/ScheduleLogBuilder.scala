package org.openurp.edu.eams.teach.schedule.log




import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import scala.collection.mutable.HashMap
import org.beangle.commons.collection.Collections



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

  private def makeInformations(lesson: Lesson): collection.mutable.Map[String, String] = {
    val empty = Collections.newMap[String, String]
    for (i <- 0 until LOG_FIELDS.length) {
      empty.put(LOG_FIELDS(i), "")
    }
    empty.put(LESSON_ID, String.valueOf(lesson.id))
    empty.put(LESSON_PROJECT, String.valueOf(lesson.project.name))
    empty.put(LESSON_SEMESTER, lesson.semester.schoolYear + "学年" + lesson.semester.name + 
      "学期")
    empty.put(LESSON_NO, lesson.no)
    empty.put(COURSE_CODE, lesson.course.code)
    empty.put(COURSE_NAME, lesson.course.name)
    empty
  }

  private def toString(informations: collection.Map[String, String]): String = {
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
    map.put(TYPE, ScheduleLogBuilder.Operation.CREATE.toString)
    map.put(DETAIL, stringify(lesson))
    if (Strings.isNotEmpty(reason)) {
      map.put(REASON, reason)
    }
    Array(toString(map), Operation.CREATE.toString)
  }

  def delete(lesson: Lesson, reason: String): Array[String] = {
    val map = makeInformations(lesson)
    map.put(TYPE, ScheduleLogBuilder.Operation.DELETE.toString)
    if (Strings.isNotEmpty(reason)) {
      map.put(REASON, reason)
    }
    Array(toString(map), Operation.DELETE.toString)
  }

  def update(lesson: Lesson, reason: String): Array[String] = {
    val map = makeInformations(lesson)
    map.put(TYPE, ScheduleLogBuilder.Operation.UPDATE.toString)
    map.put(DETAIL, stringify(lesson))
    if (Strings.isNotEmpty(reason)) {
      map.put(REASON, reason)
    }
    Array(toString(map), Operation.UPDATE.toString)
  }

  private def stringify(lesson: Lesson): String = {
    val sb = new StringBuilder()
    append(sb, "课程序号", lesson.no)
    append(sb, "学期", lesson.semester.schoolYear + "学年" + lesson.semester.name + 
      "学期")
    append(sb, "课程", lesson.course.name + '[' + lesson.course.code + 
      ']')
    append(sb, "教学项目", lesson.project.name)
    append(sb, "教学班", lesson.teachClass.name)
    val tsb = new StringBuilder()
    var iter = lesson.teachers.iterator
    while (iter.hasNext) {
      val teacher = iter.next()
      tsb.append(teacher.name + '[' + teacher.code + ']')
      if (iter.hasNext) {
        tsb.append(',')
      }
    }
    append(sb, "授课教师", tsb.toString)
    append(sb, "课程类别", lesson.courseType.name)
    append(sb, "开课院系", lesson.teachDepart.name)
    append(sb, "年级", lesson.teachClass.grade)
    append(sb, "起迄周", lesson.schedule.startWeek + "-" + lesson.schedule.endWeek + 
      "周")
    append(sb, "实际人数", lesson.teachClass.stdCount)
    append(sb, "人数上限", lesson.teachClass.limitCount)
    append(sb, "课时", lesson.schedule.period)
    append(sb, "排课结果", CourseActivityDigestor.getInstance.setDelimeter(";")
      .digest(null, lesson))
    sb.replace(sb.length - 1, sb.length, "")
    sb.toString
  }

  private def append(sb: StringBuilder, fieldName: String, value: Any) {
    if (value == null) {
      sb.append(fieldName).append("=空\n")
    } else {
      sb.append(fieldName).append('=').append(value.toString)
        .append('\n')
    }
  }
}
