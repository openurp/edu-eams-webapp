package org.openurp.edu.eams.core.service.internal

import java.sql.Date

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.edu.eams.core.service.DepartmentService

class DepartmentServiceImpl extends BaseServiceImpl with DepartmentService {

  def getDepartments(): Seq[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department]))
  }

  def getDepartment(id: java.lang.Integer): Department = {
    if (null == id) return null
    entityDao.get(classOf[Department], id)
  }

  def getColleges(): Seq[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.college=true and depart.effectiveAt <= :now and (depart.invalidAt is null or depart.invalidAt >= :now)",
      new java.util.Date())
      .orderBy("depart.code"))
  }

  def getAdministatives(): Seq[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.college=false and depart.effectiveAt <= :now and (depart.invalidAt is null or depart.invalidAt >= :now)",
      new java.util.Date())
      .orderBy("depart.code"))
  }

  def getDepartments(ids: Array[Integer]): Seq[Department] = {
    if (null == ids || ids.length < 1) List.empty else entityDao.find(classOf[Department], ids)
  }

  def getAdministatives(idSeq: String): Seq[Department] = {
    getAdministatives(Strings.transformToInteger(Strings.split(idSeq)))
  }

  def getAdministatives(ids: Array[Integer]): Seq[Department] = {
    if (null == ids || ids.length < 1) return null
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.college=false")
      .where("depart.id in (:ids)", ids))
  }

  def getColleges(idSeq: String): Seq[Department] = {
    getColleges(Strings.transformToInteger(Strings.split(idSeq)))
  }

  def getColleges(ids: Array[Integer]): Seq[Department] = {
    if (null == ids || ids.length < 1) return null
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.college=true")
      .where("depart.id in (:ids)", ids))
  }

  def getTeachDeparts(idSeq: String): Seq[Department] = {
    if (Strings.isEmpty(idSeq)) List.empty else {
      entityDao.search(OqlBuilder.from(classOf[Department], "depart").where("depart.teaching=true")
        .where("depart.id in (:ids)", Strings.transformToInt(Strings.split(idSeq))))
    }
  }

  def getRelatedDeparts(stdTypeIds: String): Iterable[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department], "depart").join("depart.stdTypes", "stdType")
      .where("stdType.id in (:stdTypeIds)", Strings.transformToInt(Strings.split(stdTypeIds)))
      .select("select distinct depart "))
  }

  def saveOrUpdate(department: Department) {
    this.entityDao.saveOrUpdate(department)
  }

  def removeDepartment(id: java.lang.Integer) {
    if (null == id) return
    entityDao.remove(classOf[Department], "id", id)
  }

  def getDepartments(idSeq: String): Seq[Department] = {
    this.getDepartments(Strings.transformToInteger(Strings.split(idSeq)))
  }

  def getTeachDeparts(): Seq[Department] = {
    entityDao.search(OqlBuilder.from(classOf[Department], "department").where("department.teaching=true")
      .where("department.effectiveAt <= :now and (department.invalidAt is null or department.invalidAt >= :now)",
        new java.util.Date())
      .orderBy("department.code"))
  }
}
