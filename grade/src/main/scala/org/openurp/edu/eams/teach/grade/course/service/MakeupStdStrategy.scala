package org.openurp.edu.eams.teach.grade.course.service

import java.util.List
import java.util.Map
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

trait MakeupStdStrategy {

  def getLessonCondition(gradeTypeId: java.lang.Integer): String

  def getCourseTakes(lesson: Lesson): List[CourseTake]

  def getCourseTakeCounts(lessons: List[Lesson]): Map[Lesson, Number]
}
