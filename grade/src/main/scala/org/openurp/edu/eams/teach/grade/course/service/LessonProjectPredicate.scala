package org.openurp.edu.eams.teach.grade.course.service

import org.apache.commons.collections.Predicate
import org.openurp.edu.base.Project
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class LessonProjectPredicate(private var project: Project) extends Predicate {

  def evaluate(`object`: AnyRef): Boolean = {
    `object`.asInstanceOf[Lesson].getProject == project
  }
}
