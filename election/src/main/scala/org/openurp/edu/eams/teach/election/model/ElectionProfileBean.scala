package org.openurp.edu.eams.teach.election.model

import java.text.ParseException
import java.util.Date


import javax.persistence.Cacheable







import javax.persistence.JoinTable
import javax.persistence.ManyToMany


import org.apache.commons.lang3.time.DateUtils
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Objects
import org.beangle.ems.rule.model.RuleConfig
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.ProjectBasedObject
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.model.Enum.ElectionProfileType




@SerialVersionUID(-6606179153423814495L)



class ElectionProfileBean extends ProjectBasedObject[Long]() with ElectionProfile {

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  @JoinTable(name = "T_ELECT_PROFILES_PROJECTS")
  
  
  var projects: Set[Project] = Collections.newSet[Any]

  
  @Enumerated(EnumType.STRING)
  
  var profileType: ElectionProfileType = ElectionProfileType.STD

  
  
  var name: String = ""

  
  
  var turn: Int = 1

  
  
  
  var semester: Semester = _

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  
  
  var stdTypes: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  @JoinTable(name = "T_ELECT_PROFILES_EDUCATIONS")
  
  
  var educations: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  
  
  var departs: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  
  
  var majors: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  
  
  var directions: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  
  
  var grades: Set[String] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  
  
  var stds: Set[Long] = Collections.newSet[Any]

  
  
  var beginAt: Date = _

  
  
  var endAt: Date = _

  
  var electBeginAt: Date = _

  
  var electEndAt: Date = _

  
  var withdrawBeginAt: Date = _

  
  var withdrawEndAt: Date = _

  
  
  var openElection: Boolean = _

  
  
  var openWithdraw: Boolean = _

  
  
  var notice: String = _

  (fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  @JoinTable(name = "T_ELECT_PROFIES_GENERAL_CFGS")
  
  
  var generalConfigs: Set[RuleConfig] = Collections.newSet[Any]

  (fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  @JoinTable(name = "T_ELECT_PROFIES_ELECT_CFGS")
  
  
  var electConfigs: Set[RuleConfig] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.LAZY)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  @JoinTable(name = "T_ELECT_PROFIES_ELECT_LESSONS")
  
  
  var electableLessons: Set[Long] = Collections.newSet[Any]

  (fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  @JoinTable(name = "T_ELECT_PROFIES_WITHDRAW_CFGS")
  
  
  var withdrawConfigs: Set[RuleConfig] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.LAZY)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  
  @JoinTable(name = "T_ELECT_PROFIES_WD_LESSONS")
  
  
  var withdrawableLessons: Set[Long] = Collections.newSet[Any]

  def this(beginDate: String, endDate: String) {
    this()
    this.beginAt = DateUtils.parseDate(beginDate, Array("yyyy-MM-dd HH:mm:ss"))
    this.endAt = DateUtils.parseDate(endDate, Array("yyyy-MM-dd HH:mm:ss"))
  }

  def this(beginAt: Date, endAt: Date) {
    super()
    this.beginAt = beginAt
    this.endAt = endAt
  }

  def isTimeSuitable(): Boolean = {
    (beginAt.getTime <= System.currentTimeMillis()) && (System.currentTimeMillis() <= endAt.getTime)
  }

  def isTimeSuitable(date: Date): Boolean = {
    (beginAt.getTime <= date.getTime) && (date.getTime <= endAt.getTime)
  }

  def isElectionTimeSuitable(date: Date): Boolean = {
    openElection && (electBeginAt.getTime <= date.getTime) && 
      (date.getTime <= electEndAt.getTime)
  }

  def isWithdrawTimeSuitable(date: Date): Boolean = {
    openWithdraw && (withdrawBeginAt.getTime <= date.getTime) && 
      (date.getTime <= withdrawEndAt.getTime)
  }

  def isOutOfDate(): Boolean = {
    System.currentTimeMillis() >= endAt.getTime
  }

  override def toString(): String = {
    Objects.toStringBuilder(this).add("semester", this.semester)
      .add("turn", turn)
      .add("beginAt", beginAt)
      .add("endAt", endAt)
      .toString
  }

  def getCheckerConfigs(): List[_] = null

  def setCheckerConfigs(checkers: List[_]) {
  }

  def getConfigs(`type`: ElectRuleType): Set[RuleConfig] = `type` match {
    case ELECTION => getElectConfigs
    case WITHDRAW => getWithdrawConfigs
    case _ => getGeneralConfigs
  }
}
