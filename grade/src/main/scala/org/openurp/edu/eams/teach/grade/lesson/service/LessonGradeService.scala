package org.openurp.edu.eams.teach.grade.lesson.service

import java.util.List
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.ExamGradeState
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

trait LessonGradeService {

  def getGradeTypes(state: CourseGradeState, userCategoryId: java.lang.Long): List[GradeType]

  def getGradeTypes(lesson: Lesson): List[GradeType]

  def getCanInputGradeTypes(isOnlyCanInput: Boolean): List[GradeType]

  def isCheckEvaluation(std: Student): Boolean

  def getState(gradeType: GradeType, gradeState: CourseGradeState, precision: java.lang.Integer): ExamGradeState
}
