package org.openurp.edu.eams.core.service



import org.openurp.base.Department



trait DepartmentService {

  def getDepartment(id: java.lang.Integer): Department

  def getDepartments(): Seq[Department]

  def getDepartments(idSeq: String): Seq[Department]

  def getDepartments(ids: Array[java.lang.Integer]): Seq[Department]

  def getColleges(): Seq[Department]

  def getColleges(idSeq: String): Seq[Department]

  def getColleges(ids: Array[java.lang.Integer]): Seq[Department]

  def getTeachDeparts(idSeq: String): Seq[Department]

  def getAdministatives(): Seq[Department]

  def getAdministatives(idSeq: String): Seq[Department]

  def getAdministatives(ids: Array[java.lang.Integer]): Seq[Department]

  def removeDepartment(id: java.lang.Integer): Unit

  def getRelatedDeparts(stdTypeIds: String): Iterable[Department]

  def saveOrUpdate(department: Department): Unit

  def getTeachDeparts(): Seq[Department]
}
