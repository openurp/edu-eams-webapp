package org.openurp.edu.eams.teach.election.service




import org.beangle.data.model.Entity
import org.beangle.ems.rule.model.RuleConfig
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.helper.RestrictionHelper
import ElectionProfileService._



object ElectionProfileService {

  val IDS_SIZE_EXCEPTION = "ids length error"
}

trait ElectionProfileService {

  def getDatas[T](clazz: Class[T], idsMap: Map[Class[_], String], project: Project): List[List[T]]

  def setDatas(profile: ElectionProfile, idsMap: Map[Class[_], String], project: Project): String

  def setRestrictionHelper(restrictionHelper: RestrictionHelper): Unit

  def getDatasMap(profiles: List[ElectionProfile]): Map[Class[_], Map[Any, Entity[_]]]

  def setElectRuleConfig(profile: ElectionProfile, configIds: Array[Integer]): Unit

  def setWithdrawRuleConfig(profile: ElectionProfile, configIds: Array[Integer]): Unit

  def setGeneralRuleConfig(profile: ElectionProfile, configIds: Array[Integer]): Unit

  def getRuleConfigs(`type`: ElectRuleType): List[RuleConfig]

  def setLessonIds(ids: Iterable[Long], lessonIds: Array[Any]): Unit

  def initDataByChance(profileId: java.lang.Long): Unit

  def getData(profileId: java.lang.Long, lessonIds: Iterable[Long]): String

  def getLastUpdateTime(profileId: java.lang.Long): String

  def getLessons(profileId: java.lang.Long): List[Lesson]

  def getProfileBySemester(semester: Semester, project: Project): List[ElectionProfile]

  def queryStdCount(profileId: java.lang.Long): String

  def getLastQueryStdCountTime(profileId: java.lang.Long): String

  def publish(): Unit
}
