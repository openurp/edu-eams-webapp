package org.openurp.edu.eams.teach.grade.service.impl

import java.util.Collection
import java.util.Date
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.teach.grade.model.StdSemesterGpa
import org.openurp.edu.eams.teach.grade.model.StdYearGpa
import org.openurp.edu.eams.teach.grade.service.CourseGradeProvider
import org.openurp.edu.eams.teach.grade.service.GpaStatService
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

class DefaultGpaStatService extends GpaStatService {

  private var courseGradeProvider: CourseGradeProvider = _

  private var gpaPolicy: GpaPolicy = _

  def statGpa(std: Student, grades: List[CourseGrade]): StdGpa = {
    val gradesMap = CollectUtils.newHashMap()
    val courseMap = CollectUtils.newHashMap()
    for (grade <- grades) {
      var semesterGrades = gradesMap.get(grade.getSemester)
      if (null == semesterGrades) {
        semesterGrades = CollectUtils.newArrayList()
        gradesMap.put(grade.getSemester, semesterGrades)
      }
      val exist = courseMap.get(grade.getCourse)
      if (null == exist || !exist.isPassed) {
        courseMap.put(grade.getCourse, grade)
      }
      semesterGrades.add(grade)
    }
    val stdGpa = new StdGpa(std)
    val yearGradeMap = CollectUtils.newHashMap()
    for (semester <- gradesMap.keySet) {
      val stdTermGpa = new StdSemesterGpa()
      stdTermGpa.setSemester(semester)
      stdGpa.add(stdTermGpa)
      val semesterGrades = gradesMap.get(semester)
      var yearGrades = yearGradeMap.get(semester.getSchoolYear)
      if (null == yearGrades) {
        yearGrades = CollectUtils.newArrayList()
        yearGradeMap.put(semester.getSchoolYear, yearGrades)
      }
      yearGrades.addAll(semesterGrades)
      stdTermGpa.setGpa(gpaPolicy.calcGpa(semesterGrades))
      stdTermGpa.setGa(gpaPolicy.calcGa(semesterGrades))
      stdTermGpa.setCount(semesterGrades.size)
      val stats = statCredits(semesterGrades)
      stdTermGpa.setTotalCredits(stats(0))
      stdTermGpa.setCredits(stats(1))
    }
    for (year <- yearGradeMap.keySet) {
      val stdYearGpa = new StdYearGpa()
      stdYearGpa.setSchoolYear(year)
      stdGpa.add(stdYearGpa)
      val yearGrades = yearGradeMap.get(year)
      stdYearGpa.setGpa(gpaPolicy.calcGpa(yearGrades))
      stdYearGpa.setGa(gpaPolicy.calcGa(yearGrades))
      stdYearGpa.setCount(yearGrades.size)
      val stats = statCredits(yearGrades)
      stdYearGpa.setTotalCredits(stats(0))
      stdYearGpa.setCredits(stats(1))
    }
    stdGpa.setGpa(gpaPolicy.calcGpa(grades))
    stdGpa.setGa(gpaPolicy.calcGa(grades))
    stdGpa.setCount(courseMap.size)
    val totalStats = statCredits(courseMap.values)
    stdGpa.setTotalCredits(totalStats(0))
    stdGpa.setCredits(totalStats(1))
    val now = new Date()
    stdGpa.setUpdatedAt(now)
    if (stdGpa.isTransient) stdGpa.setCreatedAt(now)
    stdGpa
  }

  def statGpa(std: Student, semesters: Semester*): StdGpa = {
    statGpa(std, courseGradeProvider.getPublished(std, semesters))
  }

  def statGpas(stds: Collection[Student], semesters: Semester*): MultiStdGpa = {
    val multiStdGpa = new MultiStdGpa()
    for (std <- stds) {
      multiStdGpa.getStdGpas.add(statGpa(std, semesters))
    }
    multiStdGpa.statSemestersFromStdGpa()
    multiStdGpa
  }

  private def statCredits(grades: Collection[CourseGrade]): Array[Float] = {
    var credits = 0f
    var all = 0f
    for (grade <- grades) {
      if (grade.isPassed) credits += grade.getCourse.getCredits
      all += grade.getCourse.getCredits
    }
    Array(all, credits)
  }

  def setCourseGradeProvider(provider: CourseGradeProvider) {
    this.courseGradeProvider = provider
  }

  def setGpaPolicy(gpaPolicy: GpaPolicy) {
    this.gpaPolicy = gpaPolicy
  }
}
