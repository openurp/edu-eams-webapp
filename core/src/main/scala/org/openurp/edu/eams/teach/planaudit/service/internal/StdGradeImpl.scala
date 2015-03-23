package org.openurp.edu.eams.teach.planaudit.service.internal

import org.beangle.commons.collection.Collections
import org.openurp.edu.base.Course
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.planaudit.service.StdGrade



class StdGradeImpl(courseGrades: List[CourseGrade]) extends StdGrade {

  private var gradeMap: Map[Course, List[CourseGrade]] = new HashMap[Course, List[CourseGrade]]()

  private var usedCourses: Set[Course] = new HashSet[Course]()

  private var noGradeCourses: Set[Course] = new HashSet[Course]()

  for (courseGrade <- courseGrades) {
    var gradelist = gradeMap.get(courseGrade.course)
    if (null == gradelist) {
      gradelist = new ArrayList[CourseGrade]()
      gradeMap.put(courseGrade.course, gradelist)
    }
    gradelist.add(courseGrade)
  }

  for (course <- gradeMap.keySet) {
    val gradelist = gradeMap.get(course)
    Collections.sort(gradelist)
  }

  def getGrades(course: Course): List[CourseGrade] = {
    if (noGradeCourses.contains(course)) return Collections.emptyList()
    var courseGrades = gradeMap.get(course)
    if (null == courseGrades) courseGrades = new ArrayList[CourseGrade]()
    courseGrades
  }

  def useGrades(course: Course): List[CourseGrade] = {
    if (noGradeCourses.contains(course)) {
      return Collections.emptyList()
    }
    var courseGrades = gradeMap.get(course)
    if (null == courseGrades) {
      courseGrades = new ArrayList[CourseGrade]()
    }
    usedCourses.add(course)
    courseGrades
  }

  def getRestCourses(): Iterable[Course] = {
    Collections.subtract(gradeMap.keySet, usedCourses)
  }

  def getGrades(): List[CourseGrade] = {
    val grades = new ArrayList[CourseGrade]()
    for (course <- gradeMap.keySet) {
      grades.addAll(gradeMap.get(course))
    }
    grades
  }

  def addNoGradeCourse(course: Course) {
    noGradeCourses.add(course)
  }

  def getCoursePassedMap(): Map[Long, Boolean] = {
    val passedMap = Collections.newMap()
    for (course <- gradeMap.keySet) {
      val grades = gradeMap.get(course)
      if (!grades.isEmpty) {
        val g = grades.get(0)
        passedMap.put(course.id, g.isPassed)
      }
    }
    passedMap
  }
}
