package org.openurp.edu.eams.teach.planaudit.service.internal

import org.beangle.commons.collection.Collections
import org.openurp.edu.base.Course
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.planaudit.service.StdGrade
import java.util.ArrayList
import scala.collection.mutable.Buffer

class StdGradeImpl(val grades: Seq[CourseGrade]) extends StdGrade {

  private var gradeMap = Collections.newMap[Course, Buffer[CourseGrade]]

  private var usedCourses = Collections.newSet[Course]

  private var noGradeCourses = Collections.newSet[Course]

  for (courseGrade <- grades) {
    var gradelist = gradeMap.get(courseGrade.course).orNull
    if (null == gradelist) {
      gradelist = Collections.newBuffer[CourseGrade]
      gradeMap.put(courseGrade.course, gradelist)
    }
    gradelist += (courseGrade)
  }

  for (course <- gradeMap.keySet) {
    val gradelist = gradeMap.get(course).orNull
    gradelist.sortWith((g1, g2) => g1.compare(g2) < 0)
  }

  def getGrades(course: Course): Seq[CourseGrade] = {
    if (noGradeCourses.contains(course)) return List.empty
    gradeMap.get(course).getOrElse(List.empty)
  }

  def useGrades(course: Course): Seq[CourseGrade] = {
    if (noGradeCourses.contains(course)) {
      return List.empty
    }
    val courseGrades = gradeMap.get(course).getOrElse(List.empty)
    usedCourses.add(course)
    courseGrades
  }

  def getRestCourses(): Iterable[Course] = {
    Collections.subtract(gradeMap.keySet, usedCourses)
  }

  def addNoGradeCourse(course: Course) {
    noGradeCourses.add(course)
  }

  def getCoursePassedMap(): collection.Map[java.lang.Long, Boolean] = {
    val passedMap = Collections.newMap[java.lang.Long, Boolean]
    for (course <- gradeMap.keySet) {
      val grades = gradeMap.get(course)
      if (!grades.isEmpty) {
        val g = grades.get(0)
        passedMap.put(course.id, g.passed)
      }
    }
    passedMap
  }
}
