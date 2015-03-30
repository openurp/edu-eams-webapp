package org.openurp.edu.eams.teach.program.service


import org.openurp.edu.base.Student
import org.openurp.edu.teach.plan.CourseSubstitution
import org.openurp.edu.teach.plan.MajorCourseSubstitution
import org.openurp.edu.teach.plan.StdCourseSubstitution

trait CourseSubstitutionService {

  def getCourseSubstitutions(std: Student): Seq[CourseSubstitution]

  def getMajorCourseSubstitutions(std: Student): Seq[MajorCourseSubstitution]

  def getStdCourseSubstitutions(std: Student): Seq[StdCourseSubstitution]
}
