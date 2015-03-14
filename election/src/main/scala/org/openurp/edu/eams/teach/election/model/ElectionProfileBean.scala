package org.openurp.edu.eams.teach.election.model

import java.text.ParseException
import java.util.Date
import java.util.List
import java.util.Set
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
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Objects
import org.beangle.ems.rule.model.RuleConfig
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.ProjectBasedObject
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.model.Enum.ElectionProfileType
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

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
  @BeanProperty
  var projects: Set[Project] = CollectUtils.newHashSet()

  @NotNull
  @Enumerated(EnumType.STRING)
  @BeanProperty
  var profileType: ElectionProfileType = ElectionProfileType.STD

  @NotNull
  @BeanProperty
  var name: String = ""

  @NotNull
  @BeanProperty
  var turn: Int = 1

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "STD_TYPE_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var stdTypes: Set[Integer] = CollectUtils.newHashSet()

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "EDUCATION_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFILES_EDUCATIONS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var educations: Set[Integer] = CollectUtils.newHashSet()

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "DEPART_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var departs: Set[Integer] = CollectUtils.newHashSet()

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "MAJOR_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var majors: Set[Integer] = CollectUtils.newHashSet()

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "DIRECTION_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var directions: Set[Integer] = CollectUtils.newHashSet()

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "GRADE", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var grades: Set[String] = CollectUtils.newHashSet()

  @ElementCollection(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "STD_ID", nullable = false)
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var stds: Set[Long] = CollectUtils.newHashSet()

  @NotNull
  @BeanProperty
  var beginAt: Date = _

  @NotNull
  @BeanProperty
  var endAt: Date = _

  @BeanProperty
  var electBeginAt: Date = _

  @BeanProperty
  var electEndAt: Date = _

  @BeanProperty
  var withdrawBeginAt: Date = _

  @BeanProperty
  var withdrawEndAt: Date = _

  @NotNull
  @BooleanBeanProperty
  var openElection: Boolean = _

  @NotNull
  @BooleanBeanProperty
  var openWithdraw: Boolean = _

  @NotNull
  @BeanProperty
  var notice: String = _

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "RULE_CONFIG_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_GENERAL_CFGS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var generalConfigs: Set[RuleConfig] = CollectUtils.newHashSet()

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "RULE_CONFIG_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_ELECT_CFGS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var electConfigs: Set[RuleConfig] = CollectUtils.newHashSet()

  @ElementCollection(fetch = FetchType.LAZY)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "LESSON_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_ELECT_LESSONS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var electableLessons: Set[Long] = CollectUtils.newHashSet()

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "RULE_CONFIG_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_WITHDRAW_CFGS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var withdrawConfigs: Set[RuleConfig] = CollectUtils.newHashSet()

  @ElementCollection(fetch = FetchType.LAZY)
  @JoinColumn(name = "ELECTION_PROFILE_ID")
  @Column(name = "LESSON_ID", nullable = false)
  @JoinTable(name = "T_ELECT_PROFIES_WD_LESSONS")
  @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
  @BeanProperty
  var withdrawableLessons: Set[Long] = CollectUtils.newHashSet()

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
