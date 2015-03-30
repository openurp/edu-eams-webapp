package org.openurp.edu.eams.teach.planaudit.service

import org.openurp.edu.base.Course
import org.openurp.edu.teach.grade.CourseGrade

trait StdGrade {

  def getGrades(course: Course): Seq[CourseGrade]

  def useGrades(course: Course): Seq[CourseGrade]

  def getRestCourses(): Iterable[Course]

  def grades: Seq[CourseGrade]

  def addNoGradeCourse(course: Course): Unit

  def getCoursePassedMap(): collection.Map[java.lang.Long, Boolean]
}
