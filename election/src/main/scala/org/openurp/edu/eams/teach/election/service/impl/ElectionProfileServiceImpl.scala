package org.openurp.edu.eams.teach.election.service.impl

import java.io.Serializable
import java.util.Collection
import java.util.Date
import java.util.List
import java.util.Map
import java.util.Set
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.Validate
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.beangle.commons.lang.Strings
import org.beangle.ems.rule.model.RuleConfig
import org.springframework.beans.factory.InitializingBean
import org.openurp.base.Department
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.ElectionProfileService
import org.openurp.edu.eams.teach.election.service.cache.ProfileLessonCountProvider
import org.openurp.edu.eams.teach.election.service.cache.ProfileLessonDataProvider
import org.openurp.edu.eams.teach.election.service.event.ElectionProfileChangeEvent
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.helper.RestrictionHelper

import scala.collection.JavaConversions._

class ElectionProfileServiceImpl extends BaseServiceImpl with ElectionProfileService with InitializingBean {

  private var restrictionHelper: RestrictionHelper = _

  def setLessonIds(ids: Collection[Long], lessonIds: Array[Any]) {
    if (lessonIds.length > 0) {
      val builder = OqlBuilder.from(classOf[Lesson].getName + " lesson")
      if (lessonIds.length > 500) {
        builder.where("lesson.id in(:lessonIds)", ArrayUtils.subarray(lessonIds, 0, 500))
        builder.select("lesson.id")
        ids.addAll(entityDao.search(builder))
        setLessonIds(ids, ArrayUtils.subarray(lessonIds, 500, lessonIds.length))
      } else {
        builder.where("lesson.id in(:lessonIds)", lessonIds)
        builder.select("lesson.id")
        ids.addAll(entityDao.search(builder))
      }
    }
  }

  private def addIds[T <: Serializable](ids: Collection[T], entities: Collection[_ <: Entity[T]]) {
    ids.clear()
    for (longIdEntity <- entities) {
      ids.add(longIdEntity.getId)
    }
  }

  def setDatas(profile: ElectionProfile, idsMap: Map[Class[_], String], project: Project): String = {
    for (clazz <- idsMap.keySet) {
      if (classOf[StdType] == clazz) {
        val selecteds = CollectUtils.newHashSet(getDatas(classOf[StdType], idsMap, project).get(1))
        addIds(profile.stdTypes, selecteds)
      } else if (classOf[Education] == clazz) {
        val selecteds = CollectUtils.newHashSet(getDatas(classOf[Education], idsMap, project).get(1))
        addIds(profile.educations, selecteds)
      } else if (classOf[Department] == clazz) {
        val selecteds = CollectUtils.newHashSet(getDatas(classOf[Department], idsMap, project).get(1))
        addIds(profile.getDeparts, selecteds)
      } else if (classOf[Major] == clazz) {
        val selecteds = CollectUtils.newHashSet(getDatas(classOf[Major], idsMap, project).get(1))
        addIds(profile.majors, selecteds)
      } else if (classOf[Direction] == clazz) {
        val selecteds = CollectUtils.newHashSet(getDatas(classOf[Direction], idsMap, project).get(1))
        addIds(profile.directions, selecteds)
      }
    }
    null
  }

  def getDatas[T](clazz: Class[T], idsMap: Map[Class[_], String], project: Project): List[List[T]] = {
    var selecteds = CollectUtils.newArrayList(0)
    var datas = CollectUtils.newArrayList(0)
    var idSeq: String = null
    if (null != clazz) {
      idSeq = idsMap.get(clazz)
    }
    val departs = restrictionHelper.getColleges.asInstanceOf[List[Department]]
    if ((classOf[Department] != clazz && classOf[Major] != clazz && 
      classOf[Direction] != clazz) || 
      CollectUtils.isNotEmpty(departs)) {
      val now = new Date()
      val builder = OqlBuilder.from(clazz, "data")
      if (classOf[Major] == clazz) {
        val departIds = idsMap.get(classOf[Department])
        if (Strings.isNotBlank(departIds)) {
          builder.join("data.journals", "majorDepart")
          builder.where("majorDepart.depart.id in (:departIds)", Strings.transformToInt(departIds.split(",")))
          builder.where("majorDepart.depart in(:departs)", departs)
          builder.where("data.project=:project", project)
        } else {
          builder.where("1=0")
        }
      } else if (classOf[Direction] == clazz) {
        val departIds = idsMap.get(classOf[Department])
        if (Strings.isNotBlank(departIds)) {
          builder.join("data.departs", "directionDepart")
          builder.where("directionDepart.depart.id in (:departIds)", Strings.transformToInt(departIds.split(",")))
          builder.where("directionDepart.depart in(:departs)", departs)
          builder.where("data.major.project=:project", project)
        } else {
          builder.where("1=0")
        }
        val majorIds = idsMap.get(classOf[Major])
        if (Strings.isNotBlank(majorIds)) {
          builder.where("data.major.id in (:majorIds)", Strings.transformToInt(majorIds.split(",")))
          builder.where("data.major.project=:project", project)
        } else {
          builder.where("1=0")
        }
      }
      builder.where("data.effectiveAt <= :now and (data.invalidAt is null or data.invalidAt >= :now)", 
        now)
      if (classOf[StdType] == clazz) {
        datas = restrictionHelper.stdTypes.asInstanceOf[List[T]]
        val types = CollectUtils.newHashSet(project.getTypes)
        val result = CollectUtils.newArrayList()
        for (t <- datas if types.contains(t)) {
          result.add(t)
        }
        datas = result
      } else if (classOf[Department] == clazz) {
        datas = departs.asInstanceOf[List[T]]
        val departments = CollectUtils.newHashSet(project.departments)
        val result = CollectUtils.newArrayList()
        for (t <- datas if departments.contains(t)) {
          result.add(t)
        }
        datas = result
      } else if (classOf[Education] == clazz) {
        datas = entityDao.search(builder)
        val educations = CollectUtils.newHashSet(project.educations)
        val result = CollectUtils.newArrayList()
        for (t <- datas if educations.contains(t)) {
          result.add(t)
        }
        datas = result
      } else {
        datas = entityDao.search(builder)
      }
      if (Strings.isNotBlank(idSeq)) {
        val ids = Strings.transformToInt(idSeq.split(","))
        builder.where("data.id in(:ids)", ids)
        selecteds = entityDao.search(builder)
        Validate.isTrue(ids.length == selecteds.size, IDS_SIZE_EXCEPTION)
      }
      datas.removeAll(selecteds)
    }
    CollectUtils.newArrayList(datas, selecteds)
  }

  def getRuleConfigs(`type`: ElectRuleType): List[RuleConfig] = {
    val builder = OqlBuilder.from(classOf[RuleConfig])
    builder.where("ruleConfig.enabled = true")
    builder.where("ruleConfig.rule.business =:bussiness", `type`.toString)
    entityDao.search(builder)
  }

  private def setRuleConfigs(profile: ElectionProfile, `type`: ElectRuleType, configIds: Array[Integer]) `type` match {
    case ELECTION => 
      profile.getElectConfigs.clear()
      if (configIds.length > 0) {
        profile.setElectConfigs(CollectUtils.newHashSet(entityDao.get(classOf[RuleConfig], configIds)))
      }

    case GENERAL => 
      profile.getGeneralConfigs.clear()
      if (configIds.length > 0) {
        profile.setGeneralConfigs(CollectUtils.newHashSet(entityDao.get(classOf[RuleConfig], configIds)))
      }

    case _ => 
      profile.getWithdrawConfigs.clear()
      if (configIds.length > 0) {
        profile.setWithdrawConfigs(CollectUtils.newHashSet(entityDao.get(classOf[RuleConfig], configIds)))
      }

  }

  def setElectRuleConfig(profile: ElectionProfile, configIds: Array[Integer]) {
    setRuleConfigs(profile, ElectRuleType.ELECTION, configIds)
  }

  def setWithdrawRuleConfig(profile: ElectionProfile, configIds: Array[Integer]) {
    setRuleConfigs(profile, ElectRuleType.WITHDRAW, configIds)
  }

  def setGeneralRuleConfig(profile: ElectionProfile, configIds: Array[Integer]) {
    setRuleConfigs(profile, ElectRuleType.GENERAL, configIds)
  }

  def setRestrictionHelper(restrictionHelper: RestrictionHelper) {
    this.restrictionHelper = restrictionHelper
  }

  def getDatasMap(profiles: List[ElectionProfile]): Map[Class[_], Map[Any, Entity[_]]] = {
    val departIds = CollectUtils.newHashSet()
    val majorIds = CollectUtils.newHashSet()
    val stdTypeIds = CollectUtils.newHashSet()
    val directionIds = CollectUtils.newHashSet()
    val educationIds = CollectUtils.newHashSet()
    val stdIds = CollectUtils.newHashSet()
    for (electionProfile <- profiles) {
      departIds.addAll(electionProfile.getDeparts)
      majorIds.addAll(electionProfile.majors)
      directionIds.addAll(electionProfile.directions)
      stdTypeIds.addAll(electionProfile.stdTypes)
      directionIds.addAll(electionProfile.directions)
      educationIds.addAll(electionProfile.educations)
      stdIds.addAll(electionProfile.getStds)
    }
    val result = CollectUtils.newHashMap()
    val emptyList = CollectUtils.newArrayList()
    result.put(classOf[Department], getDataMap(if (departIds.isEmpty) emptyList else entityDao.get(classOf[Department], 
      departIds)))
    result.put(classOf[Major], getDataMap(if (majorIds.isEmpty) emptyList else entityDao.get(classOf[Major], 
      majorIds)))
    result.put(classOf[StdType], getDataMap(if (stdTypeIds.isEmpty) emptyList else entityDao.get(classOf[StdType], 
      stdTypeIds)))
    result.put(classOf[Education], getDataMap(if (educationIds.isEmpty) emptyList else entityDao.get(classOf[Education], 
      educationIds)))
    result.put(classOf[Direction], getDataMap(if (directionIds.isEmpty) emptyList else entityDao.get(classOf[Direction], 
      directionIds)))
    result.put(classOf[Student], getDataMap(if (stdIds.isEmpty) emptyList else entityDao.get(classOf[Student], 
      stdIds)))
    result
  }

  private def getDataMap(entities: List[_ <: Entity[_]]): Map[Any, Entity[_]] = {
    val result = CollectUtils.newHashMap()
    for (longIdEntity <- entities) {
      result.put(longIdEntity.getId, longIdEntity)
    }
    result
  }

  def initDataByChance(profileId: java.lang.Long) {
    profileLessonDataProvider.getIdToJson(profileId)
  }

  def getData(profileId: java.lang.Long, lessonIds: Collection[Long]): String = {
    val id2json = profileLessonDataProvider.getIdToJson(profileId)
    val tmp_sb = new StringBuilder(1024 * 1024 / 2)
    tmp_sb.append("var lessonJSONs = [")
    var in = false
    for (lessonId <- lessonIds if Strings.isNotBlank(id2json.get(lessonId))) {
      tmp_sb.append(id2json.get(lessonId)).append(',')
      in = true
    }
    if (lessonIds.size > 0 && in) tmp_sb.deleteCharAt(tmp_sb.length - 1)
    tmp_sb.append("];")
    tmp_sb.toString
  }

  def getLastUpdateTime(profileId: java.lang.Long): String = {
    profileLessonDataProvider.getLastUpdateTime(profileId)
  }

  def getLessons(profileId: java.lang.Long): List[Lesson] = {
    profileLessonDataProvider.getLessons(profileId)
  }

  def getProfileBySemester(semester: Semester, project: Project): List[ElectionProfile] = {
    val builder = OqlBuilder.from(classOf[ElectionProfile], "electionProfile")
    builder.where("electionProfile.semester=:semester", semester)
    builder.where("electionProfile.project=:project", project)
    entityDao.search(builder)
  }

  private var profileLessonCountProvider: ProfileLessonCountProvider = _

  private var profileLessonDataProvider: ProfileLessonDataProvider = _

  def queryStdCount(profileId: java.lang.Long): String = {
    profileLessonCountProvider.getJson(profileId)
  }

  def getLastQueryStdCountTime(profileId: java.lang.Long): String = {
    profileLessonCountProvider.getLastUpdateTime(profileId)
  }

  def setProfileLessonCountProvider(profileLessonCountProvider: ProfileLessonCountProvider) {
    this.profileLessonCountProvider = profileLessonCountProvider
  }

  def setProfileLessonDataProvider(profileLessonDataProvider: ProfileLessonDataProvider) {
    this.profileLessonDataProvider = profileLessonDataProvider
  }

  def afterPropertiesSet() {
    new Thread(new Runnable() {

      def run() {
        profileLessonCountProvider.run()
      }
    })
      .start()
    new Thread(new Runnable() {

      def run() {
        profileLessonDataProvider.run()
      }
    })
      .start()
  }

  def publish() {
    super.publish(new ElectionProfileChangeEvent(profileLessonDataProvider))
  }
}
