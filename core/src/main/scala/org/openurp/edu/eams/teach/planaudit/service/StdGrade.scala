package org.openurp.edu.eams.teach.planaudit.service

import java.util.Collection
import java.util.List
import java.util.Map
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

trait StdGrade {

  def getGrades(course: Course): List[CourseGrade]

  def useGrades(course: Course): List[CourseGrade]

  def getRestCourses(): Collection[Course]

  def getGrades(): List[CourseGrade]

  def addNoGradeCourse(course: Course): Unit

  def getCoursePassedMap(): Map[Long, Boolean]
}
