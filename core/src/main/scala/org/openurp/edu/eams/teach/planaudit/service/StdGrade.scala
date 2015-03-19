package org.openurp.edu.eams.teach.planaudit.service




import org.openurp.edu.base.Course
import org.openurp.edu.teach.grade.CourseGrade



trait StdGrade {

  def getGrades(course: Course): List[CourseGrade]

  def useGrades(course: Course): List[CourseGrade]

  def getRestCourses(): Iterable[Course]

  def getGrades(): List[CourseGrade]

  def addNoGradeCourse(course: Course): Unit

  def getCoursePassedMap(): Map[Long, Boolean]
}
