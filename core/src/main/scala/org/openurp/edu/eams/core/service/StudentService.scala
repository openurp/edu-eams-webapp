package org.openurp.edu.eams.core.service

import java.util.Date
import org.beangle.commons.event.Event
import org.openurp.edu.eams.core.StdPerson
import org.openurp.edu.base.Student
import org.openurp.edu.base.StudentJournal
import org.openurp.edu.eams.core.code.industry.StdStatus

import scala.collection.JavaConversions._

trait StudentService {

  def getStudentByCode(code: String): Student

  def getStudent(studentId: java.lang.Long): Student

  def getStudent(code: String): Student

  def isInschool(student: Student): Boolean

  def getStdStatus(student: Student): StdStatus

  def getJournal(student: Student): StudentJournal

  def isActive(student: Student): Boolean

  def isActive(student: Student, date: Date): Boolean

  def publish(e: Event): Unit

  def getStudentByProjectAndCode(code: String, projectId: java.lang.Integer): Student

  def getMajorProjectStudent(stdPerson: StdPerson): Student

  def getMinorProjectStudent(stdPerson: StdPerson): Student
}
