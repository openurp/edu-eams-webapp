package org.openurp.edu.eams.teach.grade.service.stat

import java.util.Comparator
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Collections
import org.openurp.edu.base.Student
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.domain.GradeFilter
import org.openurp.edu.teach.grade.domain.StdGpa
import scala.collection.mutable.Buffer

class StdGrade {

  var std: Student = _

  var grades: Seq[CourseGrade] = _

  var gradeFilters: List[GradeFilter] = _

  var stdGpa: StdGpa = _

  var cmp: Ordering[CourseGrade] = _

  def toGradeMap(): collection.Map[String, CourseGrade] = {
    val gradeMap = Collections.newMap[String, CourseGrade]
    if (null == grades || grades.isEmpty) gradeMap else {
      var iter = grades.iterator
      while (iter.hasNext) {
        val courseGrade = iter.next()
        gradeMap.put(courseGrade.course.id.toString, courseGrade)
      }
      gradeMap
    }
  }

  def this(std: Student,
    courseGrades: Seq[CourseGrade],
    cmp: Ordering[CourseGrade],
    gradeFilters: List[GradeFilter]) {
    this()
    this.std = std
    this.gradeFilters = gradeFilters
    this.grades = courseGrades
    if (null != gradeFilters) {
      for (filter <- gradeFilters) {
        grades = filter.filter(grades)
      }
    }
    if (null != cmp) {
      grades.sorted(cmp)
    }
    this.cmp = cmp
  }

  def filterGrade(gradeFilter: GradeFilter) {
    if (null != gradeFilter) {
      grades = gradeFilter.filter(grades)
    }
  }

  def getCredits(): java.lang.Float = {
    if (null == grades || grades.isEmpty) {
      return new java.lang.Float(0)
    }
    var credits = 0f
    var iter = grades.iterator
    while (iter.hasNext) {
      val courseGrade = iter.next().asInstanceOf[CourseGrade]
      if (courseGrade.passed) {
        credits += courseGrade.course.credits
      }
    }
    new java.lang.Float(credits)
  }

}
