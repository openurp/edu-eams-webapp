package org.openurp.edu.eams.teach.lesson.task.dao

import java.util.List

import scala.collection.JavaConversions._

trait LessonStatDao {

  def statTeacherTitle(semesters: List[_]): List[_]
}
