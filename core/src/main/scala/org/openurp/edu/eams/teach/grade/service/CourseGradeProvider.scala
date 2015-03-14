package org.openurp.edu.eams.teach.grade.service

import java.util.Collection
import java.util.List
import java.util.Map
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

trait CourseGradeProvider {

  def getPublished(std: Student, semesters: Semester*): List[CourseGrade]

  def getAll(std: Student, semesters: Semester*): List[CourseGrade]

  def getPublished(stds: Collection[Student], semesters: Semester*): Map[Student, List[CourseGrade]]

  def getAll(stds: Collection[Student], semesters: Semester*): Map[Student, List[CourseGrade]]

  def getPassedStatus(std: Student): Map[Long, Boolean]
}
