package org.openurp.edu.eams.core.service

import org.openurp.edu.base.Student
import org.openurp.edu.base.Project
import org.openurp.edu.base.Adminclass

trait AdminclassService {

  def getAdminclass(id: java.lang.Integer): Adminclass

  def getAdminclass(code: String): Adminclass

  def saveOrUpdate(adminclass: Adminclass): Unit

  def removeAdminclass(id: java.lang.Integer): Unit

  def updateStdCount(adminclassId: java.lang.Integer): Int

  def updateActualStdCount(adminclassId: java.lang.Integer): Int

  def batchUpdateStdCountOfClass(adminclassIdSeq: String): Unit

  def batchUpdateStdCountOfClass(adminclassIds: Array[Integer]): Unit

  def batchRemoveStudentClass(students: List[_], adminclasses: List[_]): Unit

  def batchAddStudentClass(students: List[_], adminclasses: List[_]): Unit

  def updateStudentAdminclass(std: Student, adminclasses: Iterable[_], project: Project): Unit
}
