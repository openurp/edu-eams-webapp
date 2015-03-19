package org.openurp.edu.eams.teach.lesson.service




object CourseTableStyle extends Enumeration {

  val WEEK_TABLE = new CourseTableStyle()

  val UNIT_COLUMN = new CourseTableStyle()

  val LIST = new CourseTableStyle()

  class CourseTableStyle extends Val

  val STYLE_KEY = "schedule.courseTable.style"

  def getStyle(name: String): CourseTableStyle = {
    var tableStyle: CourseTableStyle = null
    if (null != name) {
      try {
        tableStyle = CourseTableStyle.valueOf(name)
      } catch {
        case e: IllegalArgumentException => 
      }
    }
    if (null == tableStyle) {
      tableStyle = CourseTableStyle.WEEK_TABLE
    }
    tableStyle
  }

  implicit def convertValue(v: Value): CourseTableStyle = v.asInstanceOf[CourseTableStyle]
}
