package org.openurp.edu.eams.teach.schedule.dao

import java.util.Collection
import org.beangle.security.blueprint.User
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

trait CourseActivityDao {

  def removeActivities(lessons: Collection[Lesson]): Unit
}
