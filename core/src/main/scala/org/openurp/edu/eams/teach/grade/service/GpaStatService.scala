package org.openurp.edu.eams.teach.grade.service

import java.util.Collection
import java.util.List
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.teach.grade.service.impl.MultiStdGpa
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

trait GpaStatService {

  def statGpa(std: Student, semesters: Semester*): StdGpa

  def statGpa(std: Student, grades: List[CourseGrade]): StdGpa

  def statGpas(stds: Collection[Student], semesters: Semester*): MultiStdGpa
}
