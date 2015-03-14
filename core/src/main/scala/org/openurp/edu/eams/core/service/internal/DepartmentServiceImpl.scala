package org.openurp.edu.eams.core.service.internal

import java.sql.Date
import java.util.Collection
import java.util.Collections
import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.edu.eams.core.service.DepartmentService

import scala.collection.JavaConversions._

class DepartmentServiceImpl extends BaseServiceImpl with DepartmentService {

  def getDepartments(): List[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department]))
  }

  def getDepartment(id: java.lang.Integer): Department = {
    if (null == id) return null
    entityDao.get(classOf[Department], id)
  }

  def getColleges(): List[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.college=true and depart.effectiveAt <= :now and (depart.invalidAt is null or depart.invalidAt >= :now)", 
      new java.util.Date())
      .orderBy("depart.code"))
  }

  def getAdministatives(): List[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.college=false and depart.effectiveAt <= :now and (depart.invalidAt is null or depart.invalidAt >= :now)", 
      new java.util.Date())
      .orderBy("depart.code"))
  }

  def getDepartments(ids: Array[Integer]): List[Department] = {
    if (null == ids || ids.length < 1) Collections.EMPTY_LIST else entityDao.get(classOf[Department], 
      ids)
  }

  def getAdministatives(idSeq: String): List[Department] = {
    getAdministatives(Strings.transformToInt(Strings.split(idSeq)))
  }

  def getAdministatives(ids: Array[Integer]): List[Department] = {
    if (null == ids || ids.length < 1) return null
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.college=false")
      .where("depart.id in (:ids)", ids))
  }

  def getColleges(idSeq: String): List[Department] = {
    getColleges(Strings.transformToInt(Strings.split(idSeq)))
  }

  def getColleges(ids: Array[Integer]): List[Department] = {
    if (null == ids || ids.length < 1) return null
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.college=true")
      .where("depart.id in (:ids)", ids))
  }

  def getTeachDeparts(idSeq: String): List[Department] = {
    if (Strings.isEmpty(idSeq)) Collections.EMPTY_LIST else {
      entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.teaching=true")
        .where("depart.id in (:ids)", Strings.transformToInt(Strings.split(idSeq))))
    }
  }

  def getRelatedDeparts(stdTypeIds: String): Collection[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").join("depart.stdTypes", "stdType")
      .where("stdType.id in (:stdTypeIds)", Strings.transformToInt(Strings.split(stdTypeIds)))
      .select("select distinct depart "))
  }

  def saveOrUpdate(department: Department) {
    if (!department.isPersisted) {
      department.setCreatedAt(new Date(System.currentTimeMillis()))
    }
    department.setUpdatedAt(new Date(System.currentTimeMillis()))
    this.entityDao.saveOrUpdate(department)
  }

  def removeDepartment(id: java.lang.Integer) {
    if (null == id) return
    entityDao.remove(classOf[Department], "id", id)
  }

  def getDepartments(idSeq: String): List[Department] = {
    this.departments(Strings.transformToInt(Strings.split(idSeq)))
  }

  def getTeachDeparts(): List[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department], "department").where("department.teaching=true")
      .where("department.effectiveAt <= :now and (department.invalidAt is null or department.invalidAt >= :now)", 
      new java.util.Date())
      .orderBy("department.code"))
  }
}
