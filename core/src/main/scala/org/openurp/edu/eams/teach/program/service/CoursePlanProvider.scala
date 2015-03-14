package org.openurp.edu.eams.teach.program.service

import java.util.Collection
import java.util.Map
import org.openurp.edu.base.Student
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.base.Program
import org.openurp.edu.eams.teach.program.StudentProgram
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.StdPlan

import scala.collection.JavaConversions._

trait CoursePlanProvider {

  def getMajorPlan(student: Student): MajorPlan

  def getMajorPlan(program: Program): MajorPlan

  def getPersonalPlan(student: Student): PersonalPlan

  def getCoursePlan(std: Student): CoursePlan

  def getCoursePlans(students: Collection[Student]): Map[Student, CoursePlan]

  def getCoursePlan(studentProgram: StudentProgram): CoursePlan
}
