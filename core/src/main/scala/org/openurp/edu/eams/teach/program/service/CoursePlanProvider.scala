package org.openurp.edu.eams.teach.program.service



import org.openurp.edu.base.Student
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.base.Program
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.StdPlan


trait CoursePlanProvider {

  def getMajorPlan(student: Student): MajorPlan

  def getMajorPlan(program: Program): MajorPlan

  def getPersonalPlan(student: Student): StdPlan

  def getCoursePlan(std: Student): CoursePlan

  def getCoursePlans(students: Iterable[Student]): Map[Student, CoursePlan]

}
