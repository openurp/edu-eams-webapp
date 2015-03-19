package org.openurp.edu.eams.teach.grade.course.dao



import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.teach.lesson.Lesson



trait CourseGradeDao {

  def needReStudy(std: Student, course: Course): Boolean

  def getGradeCourseMap(stdId: java.lang.Long): Map[Any, Boolean]

  def removeExamGrades(lesson: Lesson, gradeType: GradeType): Unit

  def publishCourseGrade(lesson: Lesson, isPublished: java.lang.Boolean): Unit

  def publishExamGrade(lesson: Lesson, gradeType: GradeType, isPublished: java.lang.Boolean): Unit

  def publishExamGrade(lesson: Lesson, gradeTypes: List[GradeType], isPublished: java.lang.Boolean): Unit

  def removeGrades(lesson: Lesson): Unit
}
