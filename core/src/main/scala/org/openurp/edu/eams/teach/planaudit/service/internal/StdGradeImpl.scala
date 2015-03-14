package org.openurp.edu.eams.teach.planaudit.service.internal

import java.util.ArrayList
import java.util.Collection
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.planaudit.service.StdGrade

import scala.collection.JavaConversions._

class StdGradeImpl(courseGrades: List[CourseGrade]) extends StdGrade {

  private var gradeMap: Map[Course, List[CourseGrade]] = new HashMap[Course, List[CourseGrade]]()

  private var usedCourses: Set[Course] = new HashSet[Course]()

  private var noGradeCourses: Set[Course] = new HashSet[Course]()

  for (courseGrade <- courseGrades) {
    var gradelist = gradeMap.get(courseGrade.getCourse)
    if (null == gradelist) {
      gradelist = new ArrayList[CourseGrade]()
      gradeMap.put(courseGrade.getCourse, gradelist)
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

  def getRestCourses(): Collection[Course] = {
    CollectUtils.subtract(gradeMap.keySet, usedCourses)
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
    val passedMap = CollectUtils.newHashMap()
    for (course <- gradeMap.keySet) {
      val grades = gradeMap.get(course)
      if (!grades.isEmpty) {
        val g = grades.get(0)
        passedMap.put(course.getId, g.isPassed)
      }
    }
    passedMap
  }
}
