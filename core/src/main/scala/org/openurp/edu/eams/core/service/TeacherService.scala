package org.openurp.edu.eams.core.service

import java.util.Collection
import java.util.List
import org.openurp.edu.base.Teacher

import scala.collection.JavaConversions._

trait TeacherService {

  def getTeacher(teacherCode: String): Teacher

  def getTeacherNamesByDepart(departmentId: java.lang.Integer): List[Array[Any]]

  def getTeacherById(id: java.lang.Long): Teacher

  def getTeacherByNO(teacherNO: String): Teacher

  def getTeachersByDepartment(departIds: String): List[Teacher]

  def getTeachersByDepartment(departIds: Array[Long]): List[Teacher]

  def getTeachersById(teacherIds: Array[java.lang.Long]): List[Teacher]

  def getTeachersById(teacherIds: Collection[_]): List[Teacher]

  def getTeachersByNO(teacherNOs: Array[String]): List[Teacher]

  def saveOrUpdate(teacher: Teacher): Unit

  def removeTeacher(id: java.lang.Long): Unit
}
