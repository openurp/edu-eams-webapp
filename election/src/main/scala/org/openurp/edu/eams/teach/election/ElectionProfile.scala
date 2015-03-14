package org.openurp.edu.eams.teach.election

import java.util.Date
import java.util.List
import java.util.Set
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.TimeEntity
import org.beangle.ems.rule.model.RuleConfig
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.model.Enum.ElectionProfileType

import scala.collection.JavaConversions._

trait ElectionProfile extends Entity[Long] with TimeEntity {

  def getProject(): Project

  def setProject(project: Project): Unit

  def getElectConfigs(): Set[RuleConfig]

  def setElectConfigs(ElectConfigs: Set[RuleConfig]): Unit

  def getBeginAt(): Date

  def setBeginAt(beginAt: Date): Unit

  def getEndAt(): Date

  def setEndAt(endAt: Date): Unit

  def getSemester(): Semester

  def setSemester(semester: Semester): Unit

  def getTurn(): Int

  def setTurn(turn: Int): Unit

  def isTimeSuitable(): Boolean

  def isTimeSuitable(date: Date): Boolean

  def isOutOfDate(): Boolean

  override def toString(): String

  def getCheckerConfigs(): List[_]

  def setCheckerConfigs(checkers: List[_]): Unit

  def getDeparts(): Set[Integer]

  def setDeparts(departs: Set[Integer]): Unit

  def getGrades(): Set[String]

  def setGrades(grades: Set[String]): Unit

  def getDirections(): Set[Integer]

  def setDirections(directions: Set[Integer]): Unit

  def getMajors(): Set[Integer]

  def setMajors(majors: Set[Integer]): Unit

  def getNotice(): String

  def setNotice(notice: String): Unit

  def getStds(): Set[Long]

  def setStds(stds: Set[Long]): Unit

  def getStdTypes(): Set[Integer]

  def setStdTypes(stdTypes: Set[Integer]): Unit

  def getEducations(): Set[Integer]

  def setEducations(educations: Set[Integer]): Unit

  def setOpenElection(openElection: Boolean): Unit

  def isOpenElection(): Boolean

  def getWithdrawConfigs(): Set[RuleConfig]

  def setWithdrawConfigs(withdrawConfigs: Set[RuleConfig]): Unit

  def getElectBeginAt(): Date

  def setElectBeginAt(electBeginAt: Date): Unit

  def getElectEndAt(): Date

  def setElectEndAt(electEndAt: Date): Unit

  def getWithdrawBeginAt(): Date

  def setWithdrawBeginAt(withdrawBeginAt: Date): Unit

  def getWithdrawEndAt(): Date

  def setWithdrawEndAt(withdrawEndAt: Date): Unit

  def getElectableLessons(): Set[Long]

  def setElectableLessons(electableLessons: Set[Long]): Unit

  def getWithdrawableLessons(): Set[Long]

  def setWithdrawableLessons(withdrawableLessons: Set[Long]): Unit

  def getGeneralConfigs(): Set[RuleConfig]

  def setGeneralConfigs(generalConfigs: Set[RuleConfig]): Unit

  def getConfigs(`type`: ElectRuleType): Set[RuleConfig]

  def isOpenWithdraw(): Boolean

  def setOpenWithdraw(openWithdraw: Boolean): Unit

  def isElectionTimeSuitable(date: Date): Boolean

  def isWithdrawTimeSuitable(date: Date): Boolean

  def getName(): String

  def setName(name: String): Unit

  def getProjects(): Set[Project]

  def setProjects(projects: Set[Project]): Unit

  def getProfileType(): ElectionProfileType

  def setProfileType(profileType: ElectionProfileType): Unit
}
