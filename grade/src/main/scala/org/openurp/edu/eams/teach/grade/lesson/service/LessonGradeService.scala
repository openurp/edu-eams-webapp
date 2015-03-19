package org.openurp.edu.eams.teach.grade.lesson.service


import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.model.ExamGradeState
import org.openurp.edu.teach.lesson.Lesson



trait LessonGradeService {

  def getGradeTypes(state: CourseGradeState, userCategoryId: java.lang.Long): List[GradeType]

  def getGradeTypes(lesson: Lesson): List[GradeType]

  def getCanInputGradeTypes(isOnlyCanInput: Boolean): List[GradeType]

  def isCheckEvaluation(std: Student): Boolean

  def getState(gradeType: GradeType, gradeState: CourseGradeState, precision: java.lang.Integer): ExamGradeState
}
