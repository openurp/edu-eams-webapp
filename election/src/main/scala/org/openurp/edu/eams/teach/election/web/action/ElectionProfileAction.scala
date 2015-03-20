package org.openurp.edu.eams.teach.election.web.action

import java.util.Arrays
import java.util.Calendarimport java.util.Date



import org.apache.commons.beanutils.PropertyUtils
import org.apache.commons.lang3.time.DateUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import org.beangle.ems.rule.RuleParameter
import org.beangle.ems.rule.model.RuleConfig
import org.beangle.ems.rule.model.RuleConfigParam
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.election.ElectPlan
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.ElectionProfileBean
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.model.Enum.ElectionProfileType
import org.openurp.edu.eams.teach.election.service.ElectionProfileService
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import com.opensymphony.xwork2.util.ArrayUtils
import ElectionProfileAction._



object ElectionProfileAction {

  object ScopeType extends Enumeration {

    val STD_TYPE = new ScopeType()

    val EDUCATION = new ScopeType()

    val DEPARTMENT = new ScopeType()

    val MAJOR = new ScopeType()

    val DIRECTION = new ScopeType()

    val STUDENT = new ScopeType()

    class ScopeType extends Val {

      def getType(): Class[_ <: Entity[_]] = this match {
        case STD_TYPE => classOf[StdType]
        case EDUCATION => classOf[Education]
        case DEPARTMENT => classOf[Department]
        case MAJOR => classOf[Major]
        case DIRECTION => classOf[Direction]
        case STUDENT => classOf[Student]
        case _ => null
      }
    }

    def value(`type`: String): ScopeType = {
      if (null == `type`) return null
      `type` = `type`.toUpperCase()
      if (`type` == STD_TYPE.toString.replace("_", "")) {
        STD_TYPE
      } else if (`type` == EDUCATION.toString.replace("_", "")) {
        EDUCATION
      } else {
        ScopeType.valueOf(`type`)
      }
    }

    implicit def convertValue(v: Value): ScopeType = v.asInstanceOf[ScopeType]
  }
}

class ElectionProfileAction extends SemesterSupportAction {

  protected var electionProfileService: ElectionProfileService = _

  protected override def getEntityName(): String = classOf[ElectionProfile].getName

  protected def indexSetting() {
    putSemester(null)
  }

  override def search(): String = {
    val profiles = entityDao.search(getQueryBuilder)
    val datasMap = electionProfileService.getDatasMap(profiles)
    put("departsMap", datasMap.get(classOf[Department]))
    put("stdTypesMap", datasMap.get(classOf[StdType]))
    put("majorsMap", datasMap.get(classOf[Major]))
    put("directionsMap", datasMap.get(classOf[Direction]))
    put(getShortName + "s", profiles)
    forward()
  }

  protected override def getQueryBuilder(): OqlBuilder[ElectionProfile] = {
    val builder = super.getQueryBuilder
    val project = getProject
    builder.where(getShortName + ".project=:project", project)
    builder.where("not exists(from " + getShortName + ".projects p where p.id<>" + 
      getShortName + 
      ".project.id )")
    builder.where(getShortName + ".semester=:semester", putSemester(null))
  }

  def copyProfile(): String = {
    val semesterId = getInt("destSemesterId")
    val profileIds = getLongIds("electionProfile")
    if (null != semesterId && ArrayUtils.isNotEmpty(profileIds)) {
      val semester = entityDao.get(classOf[Semester], semesterId)
      val origProfiles = entityDao.get(classOf[ElectionProfile], profileIds)
      val destProfiles = CollectUtils.newArrayList(origProfiles.size)
      val date = new Date()
      for (origProfile <- origProfiles) {
        val destProfile = new ElectionProfileBean()
        try {
          PropertyUtils.copyProperties(destProfile, origProfile)
          destProfile.setName(origProfile.getName + "-复制")
          destProfile.setId(null)
          destProfile.setProjects(CollectUtils.newHashSet(destProfile.getProjects))
          destProfile.setDeparts(CollectUtils.newHashSet(destProfile.getDeparts))
          destProfile.setDirections(CollectUtils.newHashSet(destProfile.directions))
          destProfile.setEducations(CollectUtils.newHashSet(destProfile.educations))
          destProfile.setElectableLessons(CollectUtils.newHashSet(destProfile.getElectableLessons))
          destProfile.setElectConfigs(CollectUtils.newHashSet(destProfile.getElectConfigs))
          destProfile.setGeneralConfigs(CollectUtils.newHashSet(destProfile.getGeneralConfigs))
          destProfile.setGrades(CollectUtils.newHashSet(destProfile.grades))
          destProfile.setMajors(CollectUtils.newHashSet(destProfile.majors))
          destProfile.setStdTypes(CollectUtils.newHashSet(destProfile.stdTypes))
          destProfile.setStds(CollectUtils.newHashSet(destProfile.getStds))
          destProfile.setWithdrawableLessons(CollectUtils.newHashSet(destProfile.getWithdrawableLessons))
          destProfile.setWithdrawConfigs(CollectUtils.newHashSet(destProfile.getWithdrawConfigs))
          destProfile.setSemester(semester)
          destProfile.setCreatedAt(date)
          destProfile.setUpdatedAt(date)
          destProfiles.add(destProfile)
        } catch {
          case e: Exception => logger.info(e.getMessage)
        }
      }
      try {
        if (!destProfiles.isEmpty) {
          entityDao.saveOrUpdate(destProfiles)
          return redirect("search", "复制成功")
        }
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
    redirect("search", "复制失败")
  }

  def edit(): String = {
    val entityId = getLongId(getShortName)
    var entity: Entity[_] = null
    entity = if (null == entityId) populateEntity() else getModel(getEntityName, entityId)
    if (null == entity) {
      return forwardError("修改的选课轮次已被删除")
    }
    put(getShortName, entity)
    editSetting(entity)
    forward()
  }

  protected def editSetting(entity: Entity[_]) {
    val profile = entity.asInstanceOf[ElectionProfile]
    val datasMap = electionProfileService.getDatasMap(CollectUtils.newArrayList(profile))
    put("departsMap", datasMap.get(classOf[Department]))
    put("stdTypesMap", datasMap.get(classOf[StdType]))
    put("majorsMap", datasMap.get(classOf[Major]))
    put("directionsMap", datasMap.get(classOf[Direction]))
    put("educationsMap", datasMap.get(classOf[Education]))
    put("stdsMap", datasMap.get(classOf[Student]))
    put("electionProfileTypes", ElectionProfileType.values)
    val lessonIds = CollectUtils.newHashSet(profile.getElectableLessons)
    lessonIds.addAll(profile.getWithdrawableLessons)
    val paramsMap = CollectUtils.newHashMap()
    val electionConfigs = electionProfileService.getRuleConfigs(ElectRuleType.ELECTION)
    for (ruleConfig <- electionConfigs; configParam <- ruleConfig.getParams) {
      paramsMap.put(configParam.getParam, configParam)
    }
    val withdrawConfigs = electionProfileService.getRuleConfigs(ElectRuleType.WITHDRAW)
    for (ruleConfig <- withdrawConfigs; configParam <- ruleConfig.getParams) {
      paramsMap.put(configParam.getParam, configParam)
    }
    val generalConfigs = electionProfileService.getRuleConfigs(ElectRuleType.GENERAL)
    for (ruleConfig <- generalConfigs; configParam <- ruleConfig.getParams) {
      paramsMap.put(configParam.getParam, configParam)
    }
    put("electionConfigs", electionConfigs)
    put("withdrawConfigs", withdrawConfigs)
    put("generalConfigs", generalConfigs)
    put("paramsMap", paramsMap)
    put("WITHDRAW", ElectRuleType.WITHDRAW)
    put("ELECTION", ElectRuleType.ELECTION)
    put("GENERAL", ElectRuleType.GENERAL)
    put("electPlans", entityDao.getAll(classOf[ElectPlan]))
    put("electRuleTypes", ElectRuleType.valueMap())
    putSemester(null)
  }

  def save(): String = {
    val profile = populateEntity(classOf[ElectionProfile], "electionProfile")
    val projectIds = getIntIds("fake.projects")
    profile.getProjects.clear()
    profile.getProjects.addAll(entityDao.get(classOf[Project], projectIds))
    profile.setBeginAt(DateUtils.truncate(profile.getBeginAt, Calendar.MINUTE))
    profile.setEndAt(DateUtils.truncate(profile.getEndAt, Calendar.MINUTE))
    profile.setElectBeginAt(DateUtils.truncate(profile.getElectBeginAt, Calendar.MINUTE))
    profile.setElectEndAt(DateUtils.truncate(profile.getElectEndAt, Calendar.MINUTE))
    profile.setWithdrawBeginAt(DateUtils.truncate(profile.getWithdrawBeginAt, Calendar.MINUTE))
    profile.setWithdrawEndAt(DateUtils.truncate(profile.getWithdrawEndAt, Calendar.MINUTE))
    val grades = get("grades")
    profile.grades.clear()
    if (Strings.isNotBlank(grades)) {
      profile.setGrades(CollectUtils.newHashSet(grades.split(",")))
    }
    val stdCodes = get("stdCodes")
    profile.getStds.clear()
    if (Strings.isNotBlank(stdCodes)) {
      val codes = CollectUtils.newHashSet(Arrays.asList(stdCodes.replace("\r\n", "\n").split("\n"):_*))
        .toArray()
      val stds = entityDao.get(classOf[Student], "code", codes)
      if (stds.size != codes.length) {
        return redirect("search", "info.save.failure")
      } else {
        profile.getStds.clear()
        for (student <- stds) {
          profile.getStds.add(student.id)
        }
      }
    }
    val idsMap = CollectUtils.newHashMap()
    idsMap.put(classOf[Education], get("educationIds"))
    idsMap.put(classOf[StdType], get("stdTypeIds"))
    idsMap.put(classOf[Department], get("departIds"))
    idsMap.put(classOf[Major], get("majorIds"))
    idsMap.put(classOf[Direction], get("directionIds"))
    profile.getElectConfigs.clear()
    val electConfigIds = getIntIds("electionConfig")
    if (ArrayUtils.isNotEmpty(electConfigIds)) {
      profile.setElectConfigs(CollectUtils.newHashSet(entityDao.get(classOf[RuleConfig], electConfigIds)))
    }
    profile.getWithdrawConfigs.clear()
    val withdrawConfigIds = getIntIds("withdrawConfig")
    if (ArrayUtils.isNotEmpty(withdrawConfigIds)) {
      profile.setWithdrawConfigs(CollectUtils.newHashSet(entityDao.get(classOf[RuleConfig], withdrawConfigIds)))
    }
    profile.getGeneralConfigs.clear()
    val generalConfigsIds = getIntIds("generalConfig")
    if (ArrayUtils.isNotEmpty(generalConfigsIds)) {
      profile.setGeneralConfigs(CollectUtils.newHashSet(entityDao.get(classOf[RuleConfig], generalConfigsIds)))
    }
    try {
      electionProfileService.setDatas(profile, idsMap, getProject)
      profile.setUpdatedAt(new Date())
      if (profile.isTransient) {
        profile.setCreatedAt(profile.getCreatedAt)
      }
      entityDao.save(profile)
      electionProfileService.publish()
    } catch {
      case e: Exception => {
        logger.error(Throwables.getStackTrace(e))
        return redirect("search", "info.save.failure")
      }
    }
    redirect("search", "info.save.success")
  }

  def ajaxQueryLessons(): String = {
    val profileId = getLong("profileId")
    put("profileId", profileId)
    val selectable = getBool("selectable")
    val `type` = getBool("dataType")
    putLessons(selectable, profileId, `type`)
    put("dataType", `type`)
    put("selectable", selectable)
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    put("courseStatusEnums", CourseStatusEnum.values)
    forward("form/ajaxQueryLessons")
  }

  protected def putLessons(selectable: Boolean, profileId: java.lang.Long, `type`: Boolean) {
    val datas = if (`type`) "electableLessons" else "withdrawableLessons"
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    val departments = getDeparts
    if (CollectUtils.isEmpty(departments)) {
      builder.where("1=2")
    } else {
      builder.where("lesson.teachDepart in(:departs)", departments)
      if (selectable) {
        if (null == profileId) {
          builder.where("lesson.semester=:semester", putSemester(null))
        } else {
          builder.where("not exists(from " + classOf[ElectionProfile].getName + 
            " electProfile " + 
            "left join electProfile." + 
            datas + 
            " lessonId where lessonId=lesson.id and electProfile.id=:profileId) and lesson.semester=:semester", 
            profileId, putSemester(null))
        }
      } else {
        if (null == profileId) {
          put("lessons", CollectUtils.newArrayList())
          return
        } else {
          builder.where("exists(from " + classOf[ElectionProfile].getName + " electProfile " + 
            "left join electProfile." + 
            datas + 
            " lessonId where lessonId=lesson.id and electProfile.id=:profileId)", profileId)
        }
      }
      var guapai = getBoolean("guapai")
      var guapaiStr = get("lesson.guapai")
      if (null == guapai && Strings.isNotBlank(guapaiStr)) {
        guapaiStr = guapaiStr.trim().toLowerCase()
        guapai = guapaiStr == "是" || guapaiStr == "true" || guapaiStr == "yes" || 
          guapaiStr == "1" || 
          guapaiStr == "y"
      }
      if (null != guapai) {
        if (guapai) {
          builder.join("lesson.tags", "lessonTag")
          builder.where("lessonTag.id=:guapaiTagId", LessonTag.PredefinedTags.GUAPAI.id)
        } else {
          builder.where("not exists(from lesson.tags lessonTag where lessonTag.id=:guapaiTagId)", LessonTag.PredefinedTags.GUAPAI.id)
        }
      }
      var electable = getBoolean("electable")
      var electableStr = get("lesson.electable")
      if (null == electable && Strings.isNotBlank(electableStr)) {
        electableStr = electableStr.trim().toLowerCase()
        electable = electableStr == "是" || electableStr == "true" || electableStr == "yes" || 
          electableStr == "1" || 
          electableStr == "y"
      }
      if (null != electable) {
        if (electable) {
          builder.join("lesson.tags", "lessonTag")
          builder.where("lessonTag.id=:electableTagId", LessonTag.PredefinedTags.ELECTABLE.id)
        } else {
          builder.where("not exists(from lesson.tags lessonTag where lessonTag.id=:electableTagId)", 
            LessonTag.PredefinedTags.ELECTABLE.id)
        }
      }
      builder.where("lesson.project=:project", getProject)
      populateConditions(builder, "lesson.id")
      val isArrangeCompleted = get("status")
      if (Strings.isNotEmpty(isArrangeCompleted)) {
        if (isArrangeCompleted == CourseStatusEnum.NEED_ARRANGE.toString) {
          builder.where("lesson.courseSchedule.status = :status", CourseStatusEnum.NEED_ARRANGE)
        } else if (isArrangeCompleted == CourseStatusEnum.DONT_ARRANGE.toString) {
          builder.where("lesson.courseSchedule.status = :status", CourseStatusEnum.DONT_ARRANGE)
        } else if (isArrangeCompleted == CourseStatusEnum.ARRANGED.toString) {
          builder.where("lesson.courseSchedule.status = :status", CourseStatusEnum.ARRANGED)
        }
      }
      builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    }
    put("lessons", entityDao.search(builder))
    put("guapaiTag", Model.newInstance(classOf[LessonTag], LessonTag.PredefinedTags.GUAPAI.id))
    put("electableTag", Model.newInstance(classOf[LessonTag], LessonTag.PredefinedTags.ELECTABLE.id))
  }

  def addOrRemoveLesson(): String = {
    val actionType = get("actionType")
    val `type` = getBool("dataType")
    put("dataType", `type`)
    val selectable = getBool("selectable")
    put("selectable", selectable)
    val profileId = getLong("profileId")
    put("profileId", profileId)
    var ids: Array[Long] = null
    val departments = getDeparts
    val profile = getModel(getEntityName, profileId).asInstanceOf[ElectionProfile]
    if (CollectUtils.isEmpty(departments)) {
      ids = Array.ofDim[Long](0)
    } else {
      val builder = OqlBuilder.from(classOf[Lesson].getName + " lesson")
      builder.where("lesson.teachDepart in(:departs)", departments)
      builder.where("lesson.semester=:semester", putSemester(null))
      builder.where("lesson.project=:project", getProject)
      if ("guapai" == actionType) {
        builder.join("lesson.tags", "lessonTag")
        builder.where("lessonTag.id=:guapaiTagId", LessonTag.PredefinedTags.GUAPAI.id)
          .select("lesson.id")
        ids = entityDao.search(builder).toArray(Array.ofDim[Long](0))
      } else if ("electable" == actionType) {
        builder.join("lesson.tags", "lessonTag")
        builder.where("lessonTag.id=:electableTagId", LessonTag.PredefinedTags.ELECTABLE.id)
          .select("lesson.id")
        ids = entityDao.search(builder).toArray(Array.ofDim[Long](0))
      } else if ("all" == actionType) {
        ids = entityDao.search(builder.select("lesson.id")).toArray(Array.ofDim[Long](0))
      } else if ("sameAsElectable" == actionType) {
        if (!profile.getWithdrawableLessons.isEmpty) {
          val lessons = entityDao.get(classOf[Lesson], profile.getWithdrawableLessons)
          val departSet = CollectUtils.newHashSet(departments)
          for (lesson <- lessons if departSet.contains(lesson.getTeachDepart)) {
            profile.getWithdrawableLessons.remove(lesson.id)
          }
        }
        if (!profile.getWithdrawableLessons.isEmpty) {
          val lessons = entityDao.get(classOf[Lesson], profile.getWithdrawableLessons)
          val departSet = CollectUtils.newHashSet(departments)
          for (lesson <- lessons if departSet.contains(lesson.getTeachDepart)) {
            profile.getWithdrawableLessons.remove(lesson.id)
          }
        }
        val idsList = CollectUtils.newArrayList()
        if (!profile.getElectableLessons.isEmpty) {
          val lessons = entityDao.get(classOf[Lesson], profile.getElectableLessons)
          val departSet = CollectUtils.newHashSet(departments)
          for (lesson <- lessons if departSet.contains(lesson.getTeachDepart)) {
            idsList.add(lesson.id)
          }
        }
        ids = idsList.toArray(Array.ofDim[Long](0))
      } else if ("clearAllWithdrawableLesson" == actionType) {
        if (!profile.getWithdrawableLessons.isEmpty) {
          val lessons = entityDao.get(classOf[Lesson], profile.getWithdrawableLessons)
          val departSet = CollectUtils.newHashSet(departments)
          for (lesson <- lessons if departSet.contains(lesson.getTeachDepart)) {
            profile.getWithdrawableLessons.remove(lesson.id)
          }
        }
        ids = Array.ofDim[Long](0)
      } else if ("clearAllElectableLesson" == actionType) {
        if (!profile.getElectableLessons.isEmpty) {
          val lessons = entityDao.get(classOf[Lesson], profile.getElectableLessons)
          val departSet = CollectUtils.newHashSet(departments)
          for (lesson <- lessons if departSet.contains(lesson.getTeachDepart)) {
            profile.getElectableLessons.remove(lesson.id)
          }
        }
        ids = Array.ofDim[Long](0)
      } else {
        val lessonIds = getLongIds("lesson")
        val idsList = CollectUtils.newArrayList()
        if (null != lessonIds && lessonIds.length > 0) {
          val lessons = entityDao.get(classOf[Lesson], lessonIds)
          val departSet = CollectUtils.newHashSet(departments)
          for (lesson <- lessons if departSet.contains(lesson.getTeachDepart)) {
            idsList.add(lesson.id)
          }
        }
        ids = idsList.toArray(Array.ofDim[Long](0))
      }
    }
    if (`type`) {
      for (id <- ids) {
        if (selectable) {
          profile.getElectableLessons.add(id)
        } else {
          profile.getElectableLessons.remove(id)
        }
      }
    } else {
      for (id <- ids) {
        if (selectable) {
          profile.getWithdrawableLessons.add(id)
        } else {
          profile.getWithdrawableLessons.remove(id)
        }
      }
    }
    profile.setUpdatedAt(new Date())
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    put("courseStatusEnums", CourseStatusEnum.values)
    val params = "&profileId=" + profileId + "&selectable=" + (if (selectable) "1" else "0") + 
      "&dataType=" + 
      (if (`type`) "1" else "0")
    try {
      entityDao.saveOrUpdate(profile)
      electionProfileService.publish()
      redirect("ajaxQueryLessons", "info.save.success", params)
    } catch {
      case e: Exception => {
        putLessons(selectable, profileId, `type`)
        redirect("ajaxQueryLessons", "info.save.failure", params)
      }
    }
  }

  def filterLessons(): String = {
    val notInIds = Strings.splitToLong(get("notInIds"))
    val inIds = Strings.splitToLong(get("inIds"))
    val guapai = getBoolean("guapai")
    val electable = getBoolean("electable")
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    if (ArrayUtils.isNotEmpty(notInIds)) {
      builder.where("lesson.id not in(:inIds)", notInIds)
    }
    if (ArrayUtils.isNotEmpty(inIds)) {
      builder.where("lesson.id in(:inIds)", inIds)
    }
    builder.where("lesson.semester=:semester", putSemester(null))
    if (null != guapai) {
      if (true == guapai) {
        builder.join("lesson.tags", "lessonTag")
        builder.where("lessonTag.id=:guapaiTagId", LessonTag.PredefinedTags.GUAPAI.id)
      } else {
        builder.where("not exists (select tag.id from lesson.tags tag where tag.id=:guapaiTagId)", LessonTag.PredefinedTags.GUAPAI.id)
      }
    }
    if (null != electable) {
      if (true == electable) {
        builder.join("lesson.tags", "lessonTag")
        builder.where("lessonTag.id=:electableTagId", LessonTag.PredefinedTags.ELECTABLE.id)
      } else {
        builder.where("not exists (select tag.id from lesson.tags tag where tag.id=:electableTagId)", 
          LessonTag.PredefinedTags.ELECTABLE.id)
      }
    }
    populateConditions(builder)
    builder.where("lesson.project=:project", getProject)
    builder.orderBy(get(Order.ORDER_STR))
    put("lessons", entityDao.search(builder))
    put("selectable", get("selectable"))
    forward("form/filterLessons")
  }

  def ajaxQuerySelectableLessons(): String = {
    val ids = Strings.splitToLong(get("selectableLessonIds"))
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    populateConditions(builder)
    builder.where("lesson.semester=:semester", putSemester(null))
    builder.where("lesson.id in(:lessonIds)", ids)
    put("lessons", entityDao.search(builder))
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    put("courseStatusEnums", CourseStatusEnum.values)
    forward("form/ajaxQueryLessonGrid")
  }

  def ajaxQuerySelectedLessons(): String = {
    val ids = Strings.splitToLong(get("selectedLessonIds"))
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    populateConditions(builder)
    builder.where("lesson.semester=:semester", putSemester(null))
    builder.where("lesson.project=:project", getProject)
    builder.where("lesson.id in(:lessonIds)", ids)
    put("lessons", entityDao.search(builder))
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    put("courseStatusEnums", CourseStatusEnum.values)
    forward("form/ajaxQueryLessonGrid")
  }

  def selectAllLessons(): String = {
    put("lessons", entityDao.get(classOf[Lesson], Array("semester", "project"), Array(putSemester(null), getProject)))
    forward()
  }

  def ajaxQuery(): String = {
    val idsMap = CollectUtils.newHashMap()
    idsMap.put(classOf[Education], get("educationIds"))
    idsMap.put(classOf[StdType], get("stdTypeIds"))
    idsMap.put(classOf[Department], get("departIds"))
    idsMap.put(classOf[Major], get("majorIds"))
    idsMap.put(classOf[Direction], get("directionIds"))
    val `type` = get("type")
    var datas: List[_] = null
    var view = "form/ajaxQuery"
    try {
      val clazz = ScopeType.value(`type`).getType
      if (classOf[Student] == clazz) {
        view = "form/ajaxQueryStd"
        val stdCodes = get("stdCodes")
        if (Strings.isNotBlank(stdCodes)) {
          val codes = stdCodes.replace("\r\n", "\n").split("\n")
          val stds = entityDao.get(classOf[Student], "code", codes)
          val stdsMap = CollectUtils.newHashMap()
          for (student <- stds) {
            stdsMap.put(student.getCode, student)
          }
          put("datas", stdsMap)
          put("selecteds", codes)
        } else {
          put("datas", CollectUtils.newHashMap())
          put("selecteds", CollectUtils.newArrayList(0))
        }
      } else {
        datas = electionProfileService.getDatas(ScopeType.value(`type`).getType, idsMap, getProject)
        put("datas", datas.get(0))
        put("selecteds", datas.get(1))
      }
    } catch {
      case e: Exception => {
        put("datas", CollectUtils.newArrayList(0))
        put("selecteds", CollectUtils.newArrayList(0))
      }
    }
    put("title", get("title"))
    put("type", `type`)
    put("now", new Date())
    forward(view)
  }

  def setElectionProfileService(electionProfileService: ElectionProfileService) {
    this.electionProfileService = electionProfileService
  }
}
