package org.openurp.edu.eams.teach.grade.service

import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState

import scala.collection.JavaConversions._

trait CourseGradeCalculator {

  def calcScore(grade: CourseGrade, state: CourseGradeState): java.lang.Float

  def calcGa(grade: CourseGrade, state: CourseGradeState): java.lang.Float

  def calc(grade: CourseGrade, state: CourseGradeState): Unit

  def updateScore(grade: CourseGrade, score: java.lang.Float): Unit

  def getGradeRateService(): GradeRateService
}
