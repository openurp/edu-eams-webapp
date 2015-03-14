package org.openurp.edu.eams.teach.grade.service

import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

trait CourseGradeService {

  def recalculate(gradeState: CourseGradeState): Unit

  def remove(task: Lesson, gradeType: GradeType): Unit

  def publish(lessonIdSeq: String, gradeTypes: Array[GradeType], isPublished: Boolean): Unit

  def getState(lesson: Lesson): CourseGradeState
}
