package org.openurp.edu.eams.teach.lesson.service




import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.lesson.Lesson



object LessonLogBuilder {

  val TYPE = "TYPE"

  val LESSON_ID = "LESSON.ID"

  val LESSON_PROJECT = "LESSON.PROJECT"

  val LESSON_SEMESTER = "LESSON.SEMESTER"

  val LESSON_NO = "LESSON.NO"

  val COURSE_CODE = "LESSON.COURSE.CODE"

  val COURSE_NAME = "LESSON.COURSE.NAME"

  val REASON = "REASON"

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
    empty.put(LESSON_ID, String.valueOf(lesson.id))
    empty.put(LESSON_PROJECT, String.valueOf(lesson.getProject.getName))
    empty.put(LESSON_SEMESTER, lesson.getSemester.getSchoolYear + "-" + lesson.getSemester.getName)
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

  def create(lesson: Lesson, reason: String): String = {
    val map = makeInformations(lesson)
    map.put(TYPE, LessonLogBuilder.Operation.CREATE.name())
    map.put(DETAIL, stringify(lesson))
    if (Strings.isNotEmpty(reason)) {
      map.put(REASON, reason)
    }
    toString(map)
  }

  def delete(lesson: Lesson, reason: String): String = {
    val map = makeInformations(lesson)
    map.put(TYPE, LessonLogBuilder.Operation.DELETE.name())
    if (Strings.isNotEmpty(reason)) {
      map.put(REASON, reason)
    }
    toString(map)
  }

  def update(lesson: Lesson, reason: String): String = {
    val map = makeInformations(lesson)
    map.put(TYPE, LessonLogBuilder.Operation.UPDATE.name())
    map.put(DETAIL, stringify(lesson))
    if (Strings.isNotEmpty(reason)) {
      map.put(REASON, reason)
    }
    toString(map)
  }

  private def stringify(lesson: Lesson): String = {
    val sb = new StringBuilder()
    append(sb, "课程序号", lesson.getNo)
    append(sb, "学期", lesson.getSemester.getSchoolYear + '-' + lesson.getSemester.getName)
    append(sb, "课程", lesson.getCourse.getName + '[' + lesson.getCourse.getCode + 
      ']')
    append(sb, "教学项目", lesson.getProject.getName)
    append(sb, "课程类别", lesson.getCourseType.getName)
    append(sb, "开课院系", lesson.getTeachDepart.getName)
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
    if (lesson.getLangType != null) {
      append(sb, "授课语言", lesson.getLangType.getName)
    } else {
      append(sb, "授课语言", null)
    }
    append(sb, "挂牌", 1)
    if (lesson.getCampus != null) {
      append(sb, "校区", lesson.getCampus.getName)
    } else {
      append(sb, "校区", null)
    }
    append(sb, "教学班", lesson.getTeachClass.getName)
    append(sb, "年级", lesson.getTeachClass.grade)
    if (lesson.getTeachClass.getDepart != null) {
      append(sb, "上课院系", lesson.getTeachClass.getDepart.getName)
    } else {
      append(sb, "上课院系", null)
    }
    append(sb, "实际人数", lesson.getTeachClass.getStdCount)
    append(sb, "人数上限", lesson.getTeachClass.getLimitCount)
    append(sb, "起始周", lesson.getCourseSchedule.getStartWeek)
    append(sb, "结束周", lesson.getCourseSchedule.getEndWeek)
    append(sb, "课时", lesson.getCourseSchedule.getPeriod)
    append(sb, "备注", lesson.getRemark)
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
