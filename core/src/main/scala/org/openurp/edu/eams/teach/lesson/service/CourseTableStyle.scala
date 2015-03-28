package org.openurp.edu.eams.teach.lesson.service

object CourseTableStyle extends Enumeration {

  val WEEK_TABLE = new CourseTableStyle()

  val UNIT_COLUMN = new CourseTableStyle()

  val LIST = new CourseTableStyle()

  class CourseTableStyle extends Val

  val STYLE_KEY = "schedule.courseTable.style"

  def getStyle(name: String): CourseTableStyle = {
    this.withName(name)
  }

  implicit def convertValue(v: Value): CourseTableStyle = v.asInstanceOf[CourseTableStyle]
}
