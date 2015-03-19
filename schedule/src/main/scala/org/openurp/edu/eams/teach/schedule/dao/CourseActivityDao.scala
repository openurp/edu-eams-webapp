package org.openurp.edu.eams.teach.schedule.dao


import org.beangle.security.blueprint.User
import org.openurp.edu.teach.lesson.Lesson



trait CourseActivityDao {

  def removeActivities(lessons: Iterable[Lesson]): Unit
}
