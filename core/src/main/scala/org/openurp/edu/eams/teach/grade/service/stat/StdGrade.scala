package org.openurp.edu.eams.teach.grade.service.stat
import java.util.Comparator
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.base.Student
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.domain.StdGpa
import org.openurp.edu.teach.grade.domain.GradeFilter
import org.beangle.commons.collection.Collections

class StdGrade {

  var std: Student = _

  var grades: List[CourseGrade] = _

  var gradeFilters: List[GradeFilter] = _

  var stdGpa: StdGpa = _

  var cmp: Comparator[CourseGrade] = _

  def toGradeMap(): Map[String, CourseGrade] = {
    val gradeMap = CollectUtils.newHashMap()
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
    courseGrades: List[CourseGrade],
    cmp: Comparator,
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
      Collections.sort(grades, cmp)
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
    var credits = 0
    var iter = grades.iterator()
    while (iter.hasNext) {
      val courseGrade = iter.next().asInstanceOf[CourseGrade]
      if (courseGrade.isPassed) {
        credits += courseGrade.course.credits
      }
    }
    new java.lang.Float(credits)
  }

  def addGrade(grade: CourseGrade) {
    this.grades.add(grade)
  }

  def getGrades(): List[CourseGrade] = grades

  def getStd(): Student = std

  def setStd(std: Student) {
    this.std = std
  }

  def getStdGpa(): StdGpa = stdGpa

  def setStdGpa(stdGpa: StdGpa) {
    this.stdGpa = stdGpa
  }

  def setGrades(grades: List[CourseGrade]) {
    this.grades = grades
  }

  def getGradeFilters(): List[GradeFilter] = gradeFilters

  def setGradeFilters(gradeFilters: List[GradeFilter]) {
    this.gradeFilters = gradeFilters
  }

  def getCmp(): Comparator[CourseGrade] = cmp

  def setCmp(cmp: Comparator[CourseGrade]) {
    this.cmp = cmp
  }
}
