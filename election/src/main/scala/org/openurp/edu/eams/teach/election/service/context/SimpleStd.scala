package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.program.StudentProgram
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-3112324608473955370L)
class SimpleStd(student: Student) extends Serializable() {

  @BeanProperty
  val id = student.getId

  @BeanProperty
  val code = student.getCode

  @BeanProperty
  val name = student.getName

  @BeanProperty
  val grade = student.grade

  @BeanProperty
  val genderId = student.getGender.getId

  @BeanProperty
  val projectId = student.getProject.getId

  @BeanProperty
  val educationId = student.education.getId

  @BeanProperty
  val stdTypeId = student.getType.getId

  @BeanProperty
  val departId = student.department.getId

  @BeanProperty
  var majorId: java.lang.Integer = if ((student.major == null)) null else student.major.getId

  @BeanProperty
  val directionId = if ((student.direction == null)) null else student.direction.getId

  @BeanProperty
  val programId = null

  @BeanProperty
  val adminclassId = if ((student.getAdminclass == null)) 0 else student.getAdminclass.getId

  @BeanProperty
  val campusId = if ((student.getCampus == null)) null else student.getCampus.getId

  def getAspectId(): java.lang.Integer = directionId

  def this(student: Student, stdProgram: StudentProgram) {
    super()
    id = student.getId
    code = student.getCode
    name = student.getName
    grade = student.grade
    genderId = student.getGender.getId
    stdTypeId = student.getType.getId
    departId = student.department.getId
    majorId = if ((student.major == null)) null else student.major.getId
    directionId = if ((student.direction == null)) null else student.direction.getId
    campusId = if ((student.getCampus == null)) null else student.getCampus.getId
    adminclassId = if ((student.getAdminclass == null)) 0 else student.getAdminclass.getId
    educationId = student.education.getId
    projectId = student.getProject.getId
    programId = if (stdProgram == null) null else stdProgram.getProgram.getId
  }
}
