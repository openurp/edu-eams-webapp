package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.program.StudentProgram




@SerialVersionUID(-3112324608473955370L)
class SimpleStd(student: Student) extends Serializable() {

  
  val id = student.id

  
  val code = student.getCode

  
  val name = student.getName

  
  val grade = student.grade

  
  val genderId = student.getGender.id

  
  val projectId = student.getProject.id

  
  val educationId = student.education.id

  
  val stdTypeId = student.getType.id

  
  val departId = student.department.id

  
  var majorId: java.lang.Integer = if ((student.major == null)) null else student.major.id

  
  val directionId = if ((student.direction == null)) null else student.direction.id

  
  val programId = null

  
  val adminclassId = if ((student.getAdminclass == null)) 0 else student.getAdminclass.id

  
  val campusId = if ((student.getCampus == null)) null else student.getCampus.id

  def getAspectId(): java.lang.Integer = directionId

  def this(student: Student, stdProgram: StudentProgram) {
    super()
    id = student.id
    code = student.getCode
    name = student.getName
    grade = student.grade
    genderId = student.getGender.id
    stdTypeId = student.getType.id
    departId = student.department.id
    majorId = if ((student.major == null)) null else student.major.id
    directionId = if ((student.direction == null)) null else student.direction.id
    campusId = if ((student.getCampus == null)) null else student.getCampus.id
    adminclassId = if ((student.getAdminclass == null)) 0 else student.getAdminclass.id
    educationId = student.education.id
    projectId = student.getProject.id
    programId = if (stdProgram == null) null else stdProgram.getProgram.id
  }
}
