package org.openurp.edu.eams.core.service

import java.sql.Date
import java.util.List
import org.openurp.edu.eams.base.Calendar
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project

import scala.collection.JavaConversions._

trait SemesterService {

  def getSemester(id: java.lang.Integer): Semester

  def getSemestersOfOverlapped(semester: Semester): List[Semester]

  def getSemester(calendar: Calendar, year: String, term: String): Semester

  def getSemester(project: Project, schoolYear: String, term: String): Semester

  def getSemester(calendar: Calendar, date: Date): Semester

  def getCurSemester(calendar: Calendar): Semester

  def getCurSemester(calendarId: java.lang.Integer): Semester

  def getNearestSemester(calendar: Calendar): Semester

  @Deprecated
  def getPreviousSemester(calendar: Calendar): Semester

  def getNextSemester(semester: Semester): Semester

  def getCurSemester(project: Project): Semester

  def getNearestSemester(project: Project): Semester

  def getTermsBetween(first: Semester, second: Semester, omitSmallTerm: Boolean): Int

  def checkDateCollision(semester: Semester): Boolean

  def removeSemester(semester: Semester): Unit

  def saveSemester(semester: Semester): Unit

  def getCalendar(project: Project): Calendar

  def getCalendars(projects: List[Project]): List[Calendar]

  def getSemesters(semesterStartId: java.lang.Integer, semesterEndId: java.lang.Integer): List[Semester]

  def getSemester(calendar: Calendar, begOn: Date, endOn: Date): Semester

  def getPrevSemester(semester: Semester): Semester
}
