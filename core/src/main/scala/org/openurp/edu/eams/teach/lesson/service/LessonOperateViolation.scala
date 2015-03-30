package org.openurp.edu.eams.teach.lesson.service

object LessonOperateViolation extends Enumeration {

  val NO_VIOLATION = new LessonOperateViolation()

  val LESSON_VIOLATION = new LessonOperateViolation()

  val PERMIT_VIOLATION = new LessonOperateViolation()

  class LessonOperateViolation extends Val

  import scala.language.implicitConversions
  implicit def convertValue(v: Value): LessonOperateViolation = v.asInstanceOf[LessonOperateViolation]
}
