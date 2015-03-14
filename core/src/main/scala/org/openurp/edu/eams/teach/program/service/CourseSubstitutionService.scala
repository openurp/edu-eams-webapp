package org.openurp.edu.eams.teach.program.service

import java.util.List
import org.openurp.edu.base.Student
import org.openurp.edu.teach.plan.CourseSubstitution
import org.openurp.edu.teach.plan.MajorCourseSubstitution
import org.openurp.edu.teach.plan.StdCourseSubstitution

import scala.collection.JavaConversions._

trait CourseSubstitutionService {

  def getCourseSubstitutions(std: Student): List[CourseSubstitution]

  def getMajorCourseSubstitutions(std: Student): List[MajorCourseSubstitution]

  def getStdCourseSubstitutions(std: Student): List[StdCourseSubstitution]
}
