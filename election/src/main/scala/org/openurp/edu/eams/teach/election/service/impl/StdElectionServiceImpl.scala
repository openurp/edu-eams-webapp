package org.openurp.edu.eams.teach.election.service.impl

import java.util.Date
import java.util.LinkedHashSet



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.Operation.Builder
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Throwables
import org.beangle.commons.text.i18n.Message
import org.beangle.ems.rule.engine.RuleExecutor
import org.beangle.ems.rule.engine.RuleExecutorBuilder
import org.beangle.ems.rule.model.RuleConfig
import org.openurp.edu.base.Student
import org.openurp.edu.eams.fee.Bill
import org.openurp.edu.eams.fee.event.BillStateChangeEvent
import org.openurp.edu.eams.fee.model.BillLogBean.BillLogType
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.dao.ElectionDao
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.model.Enum.ElectionProfileType
import org.openurp.edu.eams.teach.election.service.ElectLoggerService
import org.openurp.edu.eams.teach.election.service.ElectableLessonFilter
import org.openurp.edu.eams.teach.election.service.StdElectionService
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext.Params
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.event.ElectCourseEvent
import org.openurp.edu.eams.teach.election.service.helper.CourseLimitGroupHelper
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectBuildInPrepare
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.eams.teach.election.service.rule.PreSaveProcessor
import org.openurp.edu.eams.teach.election.service.rule.election.filter.AbstractElectableLessonFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonNoRetakeFilter
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import StdElectionServiceImpl._



object StdElectionServiceImpl {

  def getNormalTakeType(): CourseTakeType = {
    Model.newInstance(classOf[CourseTakeType], CourseTakeType.NORMAL)
  }

  def getRetakeTakeType(): CourseTakeType = {
    Model.newInstance(classOf[CourseTakeType], CourseTakeType.RESTUDY)
  }

  def getSelfMode(): ElectionMode = {
    Model.newInstance(classOf[ElectionMode], ElectionMode.SELF)
  }
}

class StdElectionServiceImpl extends BaseServiceImpl with StdElectionService {

  protected var ruleExecutorBuilder: RuleExecutorBuilder = _

  protected val profiles = CollectUtils.newHashMap()

  protected val prepares = CollectUtils.newHashMap()

  protected val filters = CollectUtils.newHashMap()

  protected val generalChecks = CollectUtils.newHashMap()

  protected val electionChecks = CollectUtils.newHashMap()

  protected val withdrawChecks = CollectUtils.newHashMap()

  protected val preSaves = CollectUtils.newHashMap()

  protected var buildInFilters: List[ElectableLessonFilter] = CollectUtils.newArrayList()

  protected var buildInGeneralChecks: List[AbstractElectRuleExecutor] = CollectUtils.newArrayList()

  protected var buildInElectionChecks: List[AbstractElectRuleExecutor] = CollectUtils.newArrayList()

  protected var buildInWithdrawChecks: List[AbstractElectRuleExecutor] = CollectUtils.newArrayList()

  protected var buildInPreSaves: List[PreSaveProcessor] = CollectUtils.newArrayList()

  protected var buildInPrepares: List[ElectBuildInPrepare] = CollectUtils.newArrayList()

  protected var electLoggerService: ElectLoggerService = _

  protected var electionDao: ElectionDao = _

  def getProfiles(std: Student): List[ElectionProfile] = {
    val builder = OqlBuilder.from(classOf[ElectionProfile], "electionProfile")
    val now = new Date()
    builder.where("electionProfile.endAt > :now", now)
    builder.where("electionProfile.profileType = :ptype", ElectionProfileType.STD)
    builder.where("exists (from electionProfile.projects p where p=:project)", std.getProject)
    builder.orderBy("electionProfile.beginAt")
    builder.cacheable(true)
    val profiles = entityDao.search(builder)
    val suitable = CollectUtils.newArrayList()
    for (profile <- profiles if isSuitable(profile, std)) {
      suitable.add(profile)
    }
    suitable
  }

  private def isSuitable(profile: ElectionProfile, std: Student): Boolean = {
    (profile.majors.isEmpty || profile.majors.contains(std.major.id)) && 
      (profile.directions.isEmpty || 
      (null != std.direction && 
      profile.directions.contains(std.direction.id))) && 
      (profile.grades.isEmpty || profile.grades.contains(std.grade)) && 
      (profile.stdTypes.isEmpty || 
      (null != std.getType && profile.stdTypes.contains(std.getType.id))) && 
      (profile.getDeparts.isEmpty || 
      profile.getDeparts.contains(std.department.id)) && 
      (profile.educations.isEmpty || 
      profile.educations.contains(std.education.id)) && 
      (profile.getStds.isEmpty || profile.getStds.contains(std.id))
  }

  def generalCheck(profile: ElectionProfile, context: ElectionCourseContext): List[Message] = {
    rebuildExecutorsNecessary(profile)
    val executors = generalChecks.get(profileKey(profile))
    for (executor <- executors) {
      executor.execute(context)
    }
    context.getMessages
  }

  def rebuildExecutorsNecessary(profile: ElectionProfile) {
    val exists = profiles.get(profileKey(profile))
    if (null == exists || exists.getUpdatedAt.before(profile.getUpdatedAt)) {
      rebuildExecutors(profile)
    }
  }

  private def profileKey(profile: ElectionProfile): String = {
    classOf[ElectionProfile].getName + profile.id.toString
  }

  private def profileKey(profileId: java.lang.Long): String = {
    classOf[ElectionProfile].getName + profileId.toString
  }

  private def rebuildExecutors(profile: ElectionProfile) {
    synchronized {
      val exists = profiles.get(profileKey(profile))
      if (null != exists && !exists.getUpdatedAt.before(profile.getUpdatedAt)) {
        return
      }
      profiles.put(profileKey(profile), profile)
      val generalExecutorSet = CollectUtils.newHashSet()
      val electionExecutorSet = CollectUtils.newHashSet()
      val withdrawExecutorSet = CollectUtils.newHashSet()
      val prepareSet = new LinkedHashSet[ElectRulePrepare]()
      val filterSet = CollectUtils.newHashSet()
      val preSaveSet = CollectUtils.newHashSet()
      val configs = profile.getGeneralConfigs
      for (config <- configs if config.isEnabled) {
        val ruleExecutor = ruleExecutorBuilder.build(config)
        if (null == ruleExecutor) //continue
        if (ruleExecutor.isInstanceOf[AbstractElectRuleExecutor]) {
          generalExecutorSet.add(ruleExecutor.asInstanceOf[AbstractElectRuleExecutor])
        }
        if (ruleExecutor.isInstanceOf[ElectRulePrepare]) {
          prepareSet.add(ruleExecutor.asInstanceOf[ElectRulePrepare])
        }
        if (ruleExecutor.isInstanceOf[PreSaveProcessor]) {
          preSaveSet.add(ruleExecutor.asInstanceOf[PreSaveProcessor])
        }
      }
      generalExecutorSet.addAll(buildInGeneralChecks)
      for (ruleConfig <- profile.getElectConfigs if ruleConfig.isEnabled) {
        val ruleExecutor = ruleExecutorBuilder.build(ruleConfig)
        if (null == ruleExecutor) //continue
        if (ruleExecutor.isInstanceOf[AbstractElectRuleExecutor]) {
          electionExecutorSet.add(ruleExecutor.asInstanceOf[AbstractElectRuleExecutor])
        }
        if (ruleExecutor.isInstanceOf[ElectRulePrepare]) {
          prepareSet.add(ruleExecutor.asInstanceOf[ElectRulePrepare])
        }
        if (ruleExecutor.isInstanceOf[ElectableLessonFilter]) {
          filterSet.add(ruleExecutor.asInstanceOf[ElectableLessonFilter])
        }
        if (ruleExecutor.isInstanceOf[PreSaveProcessor]) {
          preSaveSet.add(ruleExecutor.asInstanceOf[PreSaveProcessor])
        }
      }
      electionExecutorSet.addAll(buildInElectionChecks)
      for (ruleConfig <- profile.getWithdrawConfigs if ruleConfig.isEnabled) {
        val ruleExecutor = ruleExecutorBuilder.build(ruleConfig)
        if (null == ruleExecutor) //continue
        if (ruleExecutor.isInstanceOf[AbstractElectRuleExecutor]) {
          withdrawExecutorSet.add(ruleExecutor.asInstanceOf[AbstractElectRuleExecutor])
        }
        if (ruleExecutor.isInstanceOf[ElectRulePrepare]) {
          prepareSet.add(ruleExecutor.asInstanceOf[ElectRulePrepare])
        }
        if (ruleExecutor.isInstanceOf[ElectableLessonFilter]) {
          filterSet.add(ruleExecutor.asInstanceOf[ElectableLessonFilter])
        }
        if (ruleExecutor.isInstanceOf[PreSaveProcessor]) {
          preSaveSet.add(ruleExecutor.asInstanceOf[PreSaveProcessor])
        }
      }
      withdrawExecutorSet.addAll(buildInWithdrawChecks)
      filterSet.addAll(buildInFilters)
      preSaveSet.addAll(buildInPreSaves)
      val generalExecutors = CollectUtils.newArrayList(generalExecutorSet)
      val electionExecutors = CollectUtils.newArrayList(electionExecutorSet)
      val withdrawExecutors = CollectUtils.newArrayList(withdrawExecutorSet)
      val filterList = CollectUtils.newArrayList(filterSet)
      val preSaveList = CollectUtils.newArrayList(preSaveSet)
      Collections.sort(generalExecutors)
      Collections.sort(withdrawExecutors)
      Collections.sort(electionExecutors)
      Collections.sort(filterList.asInstanceOf[List[_]])
      Collections.sort(preSaveList.asInstanceOf[List[_]])
      generalChecks.put(profileKey(profile), generalExecutors)
      withdrawChecks.put(profileKey(profile), withdrawExecutors)
      electionChecks.put(profileKey(profile), electionExecutors)
      prepares.put(profileKey(profile), CollectUtils.newArrayList(prepareSet))
      filters.put(profileKey(profile), filterList)
      preSaves.put(profileKey(profile), preSaveList)
    }
  }

  def prepare(profile: ElectionProfile, context: PrepareContext) {
    for (take <- context.getTakes) {
      context.getState.getElectedCourseIds.put(take.getLesson.getCourse.id, take.getLesson.id)
    }
    val executors = prepares.get(profileKey(profile))
    for (executor <- executors) {
      executor.prepare(context)
    }
    for (executor <- buildInPrepares) {
      executor.asInstanceOf[ElectRulePrepare].prepare(context)
    }
  }

  def test(context: ElectionCourseContext): ElectionCourseContext = {
    val profileFilters = filters.get(profileKey(context.getState.getProfile(entityDao)))
    for (filter <- profileFilters) {
      val success = filter.asInstanceOf[AbstractElectableLessonFilter].execute(context)
      if (!success) {
        //break
      }
    }
    context
  }

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    val profileFilters = filters.get(profileKey(state.getProfileId))
    for (filter <- profileFilters) {
      val success = filter.isElectable(lesson, state)
      if (!success) {
        return false
      }
    }
    true
  }

  protected def checkElectTime(profile: ElectionProfile): Boolean = {
    val nowAt = new Date()
    if (nowAt.before(profile.getElectBeginAt)) {
      return false
    }
    if (nowAt.after(profile.getElectEndAt)) {
      return false
    }
    true
  }

  protected def executorRules(context: ElectionCourseContext, ruleExecutors: List[AbstractElectRuleExecutor]): Boolean = {
    ruleExecutors.find(ruleExecutor => null != ruleExecutor && !ruleExecutor.execute(context))
      .map(_ => false)
      .getOrElse(true)
  }

  protected def preSaveProcess(context: ElectionCourseContext, preSaveProcessors: List[PreSaveProcessor]) {
    for (p <- preSaveProcessors) {
      p.process(context)
    }
  }

  def batchOperator(contextMap: Map[ElectRuleType, List[ElectionCourseContext]]): Iterable[List[Message]] = {
    val all_toBeSavedObjectByOperation = CollectUtils.newArrayList()
    val all_toBeRemovedObjectByOperation = CollectUtils.newArrayList()
    val withdrawLessonIds = CollectUtils.newHashSet()
    val result = CollectUtils.newHashMap()
    val withdrawSuccess = CollectUtils.newHashSet()
    val electSuccess = CollectUtils.newHashSet()
    val withdrawTakes = CollectUtils.newHashSet()
    val electTakes = CollectUtils.newHashSet()
    var retakeEventSource: CourseTake = null
    var state: ElectState = null
    for (context <- contextMap.get(ElectRuleType.WITHDRAW)) {
      if (null == state) {
        state = context.getState
      }
      if (executorRules(context, withdrawChecks.get(profileKey(state.getProfile(entityDao))))) {
        val msg = tryWithdrawAndFillLog(context)
        if (null != msg) {
          context.addMessage(msg)
        } else {
          preSaveProcess(context, preSaves.get(profileKey(state.getProfile(entityDao))))
          all_toBeSavedObjectByOperation.addAll(context.getToBeSaved)
          all_toBeRemovedObjectByOperation.add(context.getCourseTake)
          all_toBeRemovedObjectByOperation.addAll(context.getToBeRemoved)
          withdrawLessonIds.add(context.getCourseTake.getLesson.id)
          withdrawSuccess.add(context.getLesson)
          withdrawTakes.add(context.getCourseTake)
          context.addMessage(new ElectMessage(null, ElectRuleType.WITHDRAW, true, context.getLesson))
        }
      }
      result.put(context.getLesson, context.getMessages)
    }
    var conflictCourseTakes: List[Lesson] = null
    for (context <- contextMap.get(ElectRuleType.ELECTION)) {
      if (null == state) {
        state = context.getState
      }
      if (state.isCheckTimeConflict) {
        if (null == conflictCourseTakes) {
          val builder = OqlBuilder.from(classOf[Lesson], "lesson")
          val electedLessonIds = state.getElectedCourseIds.values
          if (!electedLessonIds.isEmpty) {
            if (!withdrawLessonIds.isEmpty) {
              builder.where("lesson.id not in(:lessonIds)", withdrawLessonIds)
            }
            if (!electedLessonIds.isEmpty) {
              builder.where("lesson.id in(:electedLessonIds)", electedLessonIds)
              conflictCourseTakes = entityDao.search(builder)
            } else {
              conflictCourseTakes = CollectUtils.newArrayList()
            }
          }
        }
        context.getParams.put(Params.CONFLICT_COURSE_TAKES.toString, conflictCourseTakes)
      }
      val courseId = context.getLesson.getCourse.id
      val electedLessonId = state.getElectedCourseIds.get(courseId)
      if (null != electedLessonId && !withdrawLessonIds.contains(electedLessonId)) {
        context.addMessage(new ElectMessage("你已经选过" + context.getLesson.getCourse.getName, ElectRuleType.ELECTION, 
          false, context.getLesson))
        //continue
      }
      if (executorRules(context, electionChecks.get(profileKey(state.getProfile(entityDao))))) {
        val msg = tryElectAndFillLog(context)
        if (msg != null) {
          context.addMessage(msg)
        } else {
          val isRetake = true != state.getParams.get(ElectableLessonNoRetakeFilter.PARAM) && 
            state.isRetakeCourse(context.getLesson.getCourse.id)
          if (isRetake && retakeEventSource == null) {
            retakeEventSource = context.getCourseTake
          }
          preSaveProcess(context, preSaves.get(profileKey(state.getProfile(entityDao))))
          all_toBeSavedObjectByOperation.add(context.getCourseTake)
          all_toBeSavedObjectByOperation.addAll(context.getToBeSaved)
          all_toBeRemovedObjectByOperation.addAll(context.getToBeRemoved)
          electSuccess.add(context.getLesson)
          electTakes.add(context.getCourseTake)
          context.addMessage(new ElectMessage(null, ElectRuleType.ELECTION, true, context.getLesson))
        }
      }
      result.put(context.getLesson, context.getMessages)
    }
    val resultMessages = CollectUtils.newArrayList()
    var builder: Builder = null
    builder = if (!all_toBeRemovedObjectByOperation.isEmpty) Operation.remove(all_toBeRemovedObjectByOperation).saveOrUpdate(all_toBeSavedObjectByOperation) else Operation.saveOrUpdate(all_toBeSavedObjectByOperation)
    try {
      entityDao.execute(builder)
    } catch {
      case e: Exception => {
        logger.error(Throwables.getStackTrace(e))
        val messages = CollectUtils.newArrayList()
        messages.add(new ElectMessage("操作失败,请联系管理员", ElectRuleType.GENERAL, false, null))
        resultMessages.add(messages)
        var sql = "update t_lessons set std_count=std_count+1 where id=?"
        for (lesson <- withdrawSuccess) {
          electionDao.updateStdCount(sql, lesson.id)
        }
        sql = "update t_lessons set std_count=std_count-1 where id=?"
        for (lesson <- electSuccess) {
          electionDao.updateStdCount(sql, lesson.id)
        }
        sql = "update t_course_limit_groups  set cur_count=cur_count+1 where id=?"
        for (take <- withdrawTakes if null != take.getLimitGroup) {
          electionDao.updateStdCount(sql, take.getLimitGroup.id)
        }
        sql = "update t_course_limit_groups  set cur_count=cur_count-1 where id=?"
        for (take <- electTakes if null != take.getLimitGroup) {
          electionDao.updateStdCount(sql, take.getLimitGroup.id)
        }
        return resultMessages
      }
    }
    for (lesson <- withdrawSuccess) {
      state.withdrawSuccess(lesson)
    }
    for (lesson <- electSuccess) {
      state.electSuccess(lesson)
    }
    for (bill <- all_toBeSavedObjectByOperation if bill.isInstanceOf[Bill]) {
      publish(BillStateChangeEvent.create(bill.asInstanceOf[Bill], BillLogType.CANCELED))
    }
    if (null != retakeEventSource) {
      publish(ElectCourseEvent.create(retakeEventSource))
    }
    resultMessages.addAll(result.values)
    resultMessages
  }

  protected def tryElectAndFillLog(context: ElectionCourseContext): Message = {
    val state = context.getState
    val isRetake = true != state.getParams.get(ElectableLessonNoRetakeFilter.PARAM) && 
      state.isRetakeCourse(context.getLesson.getCourse.id)
    val lesson = context.getLesson
    val turn = state.getProfile(entityDao).getTurn
    val take = context.getCourseTake
    if (isRetake) {
      take.setCourseTakeType(getRetakeTakeType)
    } else {
      take.setCourseTakeType(getNormalTakeType)
    }
    val hasBeenUpdatedInLimitCountRule = state.isCheckMaxLimitCount
    if (!hasBeenUpdatedInLimitCountRule) {
      var limitGroup = take.getLimitGroup
      if (limitGroup == null) {
        limitGroup = CourseLimitGroupHelper.getMatchCourseLimitGroup(lesson, state)
      }
      if (state.isCheckTeachClass && limitGroup == null) {
        return new ElectMessage("找不到匹配的授课对象组", ElectRuleType.ELECTION, false, lesson)
      }
      var sql = "update t_lessons set std_count=std_count+1 where id=?"
      var update = electionDao.updateStdCount(sql, lesson.id)
      if (update == 0) {
        return new ElectMessage("选课失败", ElectRuleType.ELECTION, false, lesson)
      }
      if (limitGroup != null) {
        take.setLimitGroup(limitGroup)
        sql = "update t_course_limit_groups  set cur_count=cur_count+1 where id=?"
        update = electionDao.updateStdCount(sql, limitGroup.id)
        if (update == 0) {
          sql = "update t_lessons set std_count=std_count-1 where id=?"
          electionDao.updateStdCount(sql, lesson.id)
          return new ElectMessage("选课失败", ElectRuleType.ELECTION, false, lesson)
        }
      }
    }
    context.getToBeSaved.add(lesson)
    context.getToBeSaved.add(electLoggerService.genLogger(take, ElectRuleType.ELECTION, turn, take.getCreatedAt))
    null
  }

  protected def tryWithdrawAndFillLog(context: ElectionCourseContext): Message = {
    val lesson = context.getLesson
    val courseTake = context.getCourseTake
    val updateCount = !context.getState.isCheckMinLimitCount
    if (updateCount) {
      var sql = "update t_lessons  set std_count= std_count-1 where  id=?"
      val update = electionDao.updateStdCount(sql, lesson.id)
      if (update == 0) {
        return new ElectMessage("退课失败,请稍后重试", ElectRuleType.WITHDRAW, false, lesson)
      }
      if (courseTake.getLimitGroup != null) {
        sql = "update t_course_limit_groups  set cur_count=cur_count-1 where  id=?"
        electionDao.updateStdCount(sql, courseTake.getLimitGroup.id)
      }
    }
    context.getToBeSaved.add(lesson)
    context.getToBeSaved.add(electLoggerService.genLogger(courseTake, ElectRuleType.WITHDRAW))
    null
  }

  def setBuildInPrepares(buildInPrepares: List[ElectBuildInPrepare]) {
    this.buildInPrepares = buildInPrepares
  }

  def setElectLoggerService(electLoggerService: ElectLoggerService) {
    this.electLoggerService = electLoggerService
  }

  def setElectionDao(electionDao: ElectionDao) {
    this.electionDao = electionDao
  }

  def getElectionDao(): ElectionDao = electionDao

  def setRuleExecutorBuilder(ruleExecutorBuilder: RuleExecutorBuilder) {
    this.ruleExecutorBuilder = ruleExecutorBuilder
  }

  def setBuildInFilters(buildInFilters: List[ElectableLessonFilter]) {
    this.buildInFilters = buildInFilters
  }

  def setBuildInGeneralChecks(buildInGeneralChecks: List[AbstractElectRuleExecutor]) {
    this.buildInGeneralChecks = buildInGeneralChecks
  }

  def setBuildInElectionChecks(buildInElectionChecks: List[AbstractElectRuleExecutor]) {
    this.buildInElectionChecks = buildInElectionChecks
  }

  def setBuildInWithdrawChecks(buildInWithdrawChecks: List[AbstractElectRuleExecutor]) {
    this.buildInWithdrawChecks = buildInWithdrawChecks
  }

  def setBuildInPreSaves(buildInPreSaves: List[PreSaveProcessor]) {
    this.buildInPreSaves = buildInPreSaves
  }
}
