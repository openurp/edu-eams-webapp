package org.openurp.edu.eams.core.service



import org.openurp.edu.base.Teacher



trait TeacherService {

  def getTeacher(teacherCode: String): Teacher

  def getTeacherNamesByDepart(departmentId: java.lang.Integer): Seq[Array[Any]]

  def getTeacherById(id: java.lang.Long): Teacher

  def getTeacherByNO(teacherNO: String): Teacher

  def getTeachersByDepartment(departIds: String): Seq[Teacher]

  def getTeachersByDepartment(departIds: Array[java.lang.Long]): Seq[Teacher]

  def getTeachersById(teacherIds: Array[java.lang.Long]): Seq[Teacher]

  def getTeachersById(teacherIds: Iterable[_]): Seq[Teacher]

  def getTeachersByNO(teacherNOs: Array[String]): Seq[Teacher]

  def saveOrUpdate(teacher: Teacher): Unit

  def removeTeacher(id: java.lang.Long): Unit
}
