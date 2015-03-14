package org.openurp.edu.eams.teach.election

import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.TimeEntity
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.teach.lesson.CourseTake

import scala.collection.JavaConversions._

trait ElectLogger extends Entity[Long] with TimeEntity {

  def getSemester(): Semester

  def setSemester(semester: Semester): Unit

  def getLessonNo(): String

  def setLessonNo(lessonNo: String): Unit

  def getCourseType(): String

  def setCourseType(courseType: String): Unit

  def getCourseCode(): String

  def setCourseCode(courseCode: String): Unit

  def getCourseName(): String

  def setCourseName(courseName: String): Unit

  def getCredits(): Float

  def setCredits(credits: Float): Unit

  def getTurn(): java.lang.Integer

  def setTurn(turn: java.lang.Integer): Unit

  def getStdCode(): String

  def setStdCode(stdCode: String): Unit

  def getStdName(): String

  def setStdName(stdName: String): Unit

  def getOperatorCode(): String

  def setOperatorCode(operatorCode: String): Unit

  def getOperatorName(): String

  def setOperatorName(operatorName: String): Unit

  def getIpAddress(): String

  def setIpAddress(ipAddress: String): Unit

  def getType(): ElectRuleType

  def setType(`type`: ElectRuleType): Unit

  def getCourseTakeType(): CourseTakeType

  def setCourseTakeType(courseTakeType: CourseTakeType): Unit

  def setLoggerData(courseTake: CourseTake): Unit

  def setElectionMode(electionMode: ElectionMode): Unit

  def getElectionMode(): ElectionMode

  def getProject(): Project

  def setProject(project: Project): Unit
}
