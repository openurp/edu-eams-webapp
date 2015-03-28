package org.openurp.edu.eams.teach.election.model

import java.text.ParseException
import java.util.Date


import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
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
@Entity(name = "org.openurp.edu.eams.teach.election.ElectionProfile")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
class ElectionProfileBean extends ProjectBasedObject[Long]() with ElectionProfile {

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "PROJECT_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFILES_PROJECTS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var projects: Set[Project] = Collections.newSet[Any]

  @NotNull
  @Enumerated(EnumType.STRING)
  
  var profileType: ElectionProfileType = ElectionProfileType.STD

  @NotNull
  
  var name: String = ""

  @NotNull
  
  var turn: Int = 1

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "STD_TYPE_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var stdTypes: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "EDUCATION_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFILES_EDUCATIONS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var educations: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "DEPART_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var departs: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "MAJOR_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var majors: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "DIRECTION_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var directions: Set[Integer] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "GRADE", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var grades: Set[String] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "STD_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var stds: Set[Long] = Collections.newSet[Any]

  @NotNull
  
  var beginAt: Date = _

  @NotNull
  
  var endAt: Date = _

  
  var electBeginAt: Date = _

  
  var electEndAt: Date = _

  
  var withdrawBeginAt: Date = _

  
  var withdrawEndAt: Date = _

  @NotNull
  
  var openElection: Boolean = _

  @NotNull
  
  var openWithdraw: Boolean = _

  @NotNull
  
  var notice: String = _

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "RULE_CONFIG_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_GENERAL_CFGS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var generalConfigs: Set[RuleConfig] = Collections.newSet[Any]

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "RULE_CONFIG_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_ELECT_CFGS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var electConfigs: Set[RuleConfig] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.LAZY)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "LESSON_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_ELECT_LESSONS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var electableLessons: Set[Long] = Collections.newSet[Any]

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "RULE_CONFIG_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_WITHDRAW_CFGS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
  var withdrawConfigs: Set[RuleConfig] = Collections.newSet[Any]

  @ElementCollection(fetch = FetchType.LAZY)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "LESSON_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_WD_LESSONS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  
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
