package org.openurp.edu.eams.core.service

import org.openurp.edu.base.Student
import org.openurp.edu.base.Project
import org.openurp.edu.base.Adminclass

trait AdminclassService {

  def getAdminclass(id: java.lang.Long): Adminclass

  def getAdminclass(code: String): Adminclass

  def saveOrUpdate(adminclass: Adminclass): Unit

  def removeAdminclass(id: java.lang.Long): Unit

  def updateStdCount(adminclassId: java.lang.Long): Int

  def updateActualStdCount(adminclassId: java.lang.Long): Int

  def batchUpdateStdCountOfClass(adminclassIdSeq: String): Unit

  def batchUpdateStdCountOfClass(adminclassIds: Array[java.lang.Long]): Unit

  def batchRemoveStudentClass(students: Seq[_], adminclasses: Seq[_]): Unit

  def batchAddStudentClass(students: Seq[_], adminclasses: Seq[_]): Unit

  def updateStudentAdminclass(std: Student, adminclasses: Iterable[Adminclass], project: Project): Unit
}
