package org.openurp.edu.eams.teach.program.service


import org.openurp.edu.base.Student
import org.openurp.edu.base.Program



trait StudentProgramBindService {

  def unbind(student: Student): Unit

  def autobind(student: Student, withStdType: Boolean, withDirection: Boolean): Unit

  def bind(student: Student, program: Program): Unit

  def forcebind(student: Student, program: Program): Unit

  def guessMajorPrograms(student: Student, withStdType: Boolean, withDirection: Boolean): List[Program]

  def matchMajorProgram(student: Student, withStdType: Boolean, withDirection: Boolean): Program
}
