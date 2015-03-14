package org.openurp.edu.eams.core.service.internal

import java.util.Date
import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.core.StdPerson
import org.openurp.edu.base.Student
import org.openurp.edu.base.StudentJournal
import org.openurp.edu.eams.core.code.industry.StdStatus
import org.openurp.edu.eams.core.service.StudentService

import scala.collection.JavaConversions._

class StudentServiceImpl extends BaseServiceImpl with StudentService {

  def getStudent(studentId: java.lang.Long): Student = {
    entityDao.get(classOf[Student], studentId)
  }

  def getStudent(code: String): Student = {
    val list = entityDao.get(classOf[Student], "code", code)
    if (list.isEmpty) {
      null
    } else {
      list.get(0)
    }
  }

  def getJournal(student: Student): StudentJournal = {
    var builder = OqlBuilder.from(classOf[StudentJournal], "stdJournal")
      .where("stdJournal.std = :std and stdJournal.beginOn<=:now and stdJournal.endOn>=:now", student, 
      new java.sql.Date(System.currentTimeMillis()))
      .orderBy("stdJournal.id desc")
    var rs = entityDao.search(builder)
    if (rs.isEmpty) {
      builder = OqlBuilder.from(classOf[StudentJournal], "stdJournal")
        .where("stdJournal.std = :std and stdJournal.beginOn > :now", student, new java.sql.Date(System.currentTimeMillis()))
        .orderBy("stdJournal.id")
      rs = entityDao.search(builder)
    }
    if (rs.isEmpty) null else rs.get(0)
  }

  def isInschool(student: Student): Boolean = {
    val journal = getJournal(student)
    if (journal == null) false else journal.isInschool
  }

  def stdExists(code: String): Boolean = {
    entityDao.count(classOf[Student], "code", code) == 1
  }

  def isActive(student: Student): Boolean = isActive(student, new Date())

  def isActive(student: Student, date: Date): Boolean = {
    student.isRegisted && student.getRegistOn.before(date) && 
      student.getGraduateOn.after(date)
  }

  def getStdStatus(student: Student): StdStatus = {
    val journal = getJournal(student)
    if (journal != null) journal.getStatus else null
  }

  def getStudentByCode(code: String): Student = {
    val list = entityDao.get(classOf[Student], "code", code)
    if (list.isEmpty) {
      null
    } else {
      list.get(0).asInstanceOf[Student]
    }
  }

  def getStudentByProjectAndCode(code: String, projectId: java.lang.Integer): Student = {
    val builder = OqlBuilder.from(classOf[Student], "student")
    builder.where("student.code =:code", code)
    if (null != projectId) {
      builder.where("student.project.id=:projectId", projectId)
    }
    builder.orderBy("student.code").limit(1, 10)
    val students = entityDao.search(builder)
    if (students.size > 0) {
      return students.get(0)
    }
    null
  }

  def getMajorProjectStudent(stdPerson: StdPerson): Student = {
    val query = OqlBuilder.from(classOf[Student], "std")
    query.where("std.person = :stdPerson", stdPerson).where("std.project.minor = false")
    entityDao.uniqueResult(query)
  }

  def getMinorProjectStudent(stdPerson: StdPerson): Student = {
    val query = OqlBuilder.from(classOf[Student], "std")
    query.where("std.person = :stdPerson", stdPerson).where("std.project.minor = true")
    entityDao.uniqueResult(query)
  }
}
