package org.openurp.edu.eams.teach.schedule.service

import org.beangle.commons.text.i18n.Message
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.Lesson

trait LessonScheduleChecker {

  def check(lesson: Lesson, activities: Iterable[CourseActivity]): Message
}
