package org.openurp.edu.eams.teach.schedule.service

import java.util.List
import org.beangle.commons.text.i18n.Message
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

trait LessonScheduleChecker {

  def check(lesson: Lesson, activities: List[CourseActivity]): Message
}
