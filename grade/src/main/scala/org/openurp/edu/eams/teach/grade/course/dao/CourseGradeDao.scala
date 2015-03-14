package org.openurp.edu.eams.teach.grade.course.dao

import java.util.List
import java.util.Map
import org.openurp.edu.base.Student
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

trait CourseGradeDao {

  def needReStudy(std: Student, course: Course): Boolean

  def getGradeCourseMap(stdId: java.lang.Long): Map[Any, Boolean]

  def removeExamGrades(lesson: Lesson, gradeType: GradeType): Unit

  def publishCourseGrade(lesson: Lesson, isPublished: java.lang.Boolean): Unit

  def publishExamGrade(lesson: Lesson, gradeType: GradeType, isPublished: java.lang.Boolean): Unit

  def publishExamGrade(lesson: Lesson, gradeTypes: List[GradeType], isPublished: java.lang.Boolean): Unit

  def removeGrades(lesson: Lesson): Unit
}
