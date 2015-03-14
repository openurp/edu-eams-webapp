package org.openurp.edu.eams.core.service

import java.util.Collection
import java.util.List
import org.openurp.base.Department

import scala.collection.JavaConversions._

trait DepartmentService {

  def getDepartment(id: java.lang.Integer): Department

  def getDepartments(): List[Department]

  def getDepartments(idSeq: String): List[Department]

  def getDepartments(ids: Array[java.lang.Integer]): List[Department]

  def getColleges(): List[Department]

  def getColleges(idSeq: String): List[Department]

  def getColleges(ids: Array[java.lang.Integer]): List[Department]

  def getTeachDeparts(idSeq: String): List[Department]

  def getAdministatives(): List[Department]

  def getAdministatives(idSeq: String): List[Department]

  def getAdministatives(ids: Array[java.lang.Integer]): List[Department]

  def removeDepartment(id: java.lang.Integer): Unit

  def getRelatedDeparts(stdTypeIds: String): Collection[Department]

  def saveOrUpdate(department: Department): Unit

  def getTeachDeparts(): List[Department]
}
