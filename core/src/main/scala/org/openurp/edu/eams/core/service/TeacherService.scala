package org.openurp.edu.eams.core.service



import org.openurp.edu.base.Teacher



trait TeacherService {

  def getTeacher(teacherCode: String): Teacher

  def getTeacherNamesByDepart(departmentId: java.lang.Integer): List[Array[Any]]

  def getTeacherById(id: java.lang.Long): Teacher

  def getTeacherByNO(teacherNO: String): Teacher

  def getTeachersByDepartment(departIds: String): List[Teacher]

  def getTeachersByDepartment(departIds: Array[Long]): List[Teacher]

  def getTeachersById(teacherIds: Array[java.lang.Long]): List[Teacher]

  def getTeachersById(teacherIds: Iterable[_]): List[Teacher]

  def getTeachersByNO(teacherNOs: Array[String]): List[Teacher]

  def saveOrUpdate(teacher: Teacher): Unit

  def removeTeacher(id: java.lang.Long): Unit
}
