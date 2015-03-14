package org.openurp.edu.eams.teach.grade.service

import java.util.List
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

trait GpaService {

  def getGpa(std: Student): java.lang.Float

  def getGpa(std: Student, grades: List[CourseGrade]): java.lang.Float

  def getGpa(std: Student, semester: Semester): java.lang.Float
}
