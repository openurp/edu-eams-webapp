package org.openurp.edu.eams.teach.grade.service.impl

import java.util.List
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

trait GpaPolicy {

  def calcGpa(grades: List[CourseGrade]): java.lang.Float

  def calcGa(grades: List[CourseGrade]): java.lang.Float

  def round(gpa: java.lang.Float): java.lang.Float

  def getPrecision(): Int
}
