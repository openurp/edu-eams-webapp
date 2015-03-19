package org.openurp.edu.eams.teach.election.service.propertyExtractor


import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.transfer.exporter.DefaultPropertyExtractor
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.teach.lesson.CourseSchedule
import org.openurp.edu.teach.lesson.CourseTake



class CourseTakePropertyExtractor(textResource: TextResource) extends DefaultPropertyExtractor(textResource) {

  def getPropertyValue(target: AnyRef, property: String): AnyRef = {
    if ("teachers" == property) {
      val courseTake = target.asInstanceOf[CourseTake]
      val builder = new StringBuilder()
      val it = courseTake.getLesson.getTeachers.iterator()
      if (!courseTake.getLesson.getTeachers.isEmpty) {
        while (true) {
          builder.append(it.next().getName)
          if (it.hasNext) {
            builder.append(",")
          } else {
            //break
          }
        }
      }
      builder.toString
    } else if ("lesson.courseSchedule" == property) {
      val courseSchedule = target.asInstanceOf[CourseTake].getLesson.getCourseSchedule
      val builder = new StringBuilder()
      if (null != courseSchedule) {
        builder.append(courseSchedule.getStartWeek).append("-")
          .append(courseSchedule.getEndWeek)
          .append("周")
        builder.append(courseSchedule.getPeriod).append("课时")
      }
      builder.toString
    } else {
      super.getPropertyValue(target, property)
    }
  }
}
