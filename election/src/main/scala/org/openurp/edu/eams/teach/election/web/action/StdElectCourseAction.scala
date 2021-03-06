package org.openurp.edu.eams.teach.election.web.action

import java.io.IOException
import java.io.PrintWriter

import java.util.Comparator
import java.util.Date




import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Predicate
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.SinglePage
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Triple
import org.beangle.commons.text.i18n.Message
import org.beangle.ems.rule.engine.RuleExecutorBuilder
import org.beangle.ems.rule.model.RuleConfig
import org.beangle.security.blueprint.service.UserService
import org.openurp.base.Campus
import org.openurp.edu.eams.base.CourseUnit
import org.openurp.base.Semester
import org.openurp.edu.eams.base.model.CampusBean
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.core.model.ProjectBean
import org.openurp.edu.eams.core.model.StudentBean
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.election.CourseTypeCreditConstraint
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.ElectionProfileBean
import org.openurp.edu.eams.teach.election.model.StdApplyLog
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.CourseTakeService
import org.openurp.edu.eams.teach.election.service.ElectionProfileService
import org.openurp.edu.eams.teach.election.service.StdElectionService
import org.openurp.edu.eams.teach.election.service.context.ElectCourseSubstitution
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext.Params
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.SimpleStd
import org.openurp.edu.eams.teach.election.service.helper.CourseTakeHelper
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.service.CourseTableStyle
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.teach.planaudit.CourseAuditResult
import org.openurp.edu.teach.planaudit.GroupAuditResult
import org.openurp.edu.teach.planaudit.PlanAuditResult
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditContext
import org.openurp.edu.eams.teach.planaudit.service.PlanAuditService
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.StudentProgram
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import org.openurp.edu.eams.teach.program.share.SharePlanCourseGroup
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.web.action.common.AbstractStudentProjectSupportAction
import com.opensymphony.xwork2.ActionContext
import StdElectCourseAction._



object StdElectCourseAction {

  def getNormalTakeType(): CourseTakeType = {
    Model.newInstance(classOf[CourseTakeType], CourseTakeType.NORMAL)
  }

  def getSelfMode(): ElectionMode = {
    Model.newInstance(classOf[ElectionMode], ElectionMode.SELF)
  }

  protected var HEADER_ETAG: String = "ETag"

  protected var HEADER_IF_NONE_MATCH: String = "If-None-Match"
}

class StdElectCourseAction extends AbstractStudentProjectSupportAction {

   var electionProfileService: ElectionProfileService = _

   var coursePlanProvider: CoursePlanProvider = _

   var lessonSearchHelper: LessonSearchHelper = _

   var lessonService: LessonService = _

   var stdElectionService: StdElectionService = _

   var userService: UserService = _

   var planAuditService: PlanAuditService = _

   var courseTakeService: CourseTakeService = _

   var timeSettingService: TimeSettingService = _

   var ruleExecutorBuilder: RuleExecutorBuilder = _

  override def innerIndex(): String = {
    val std = getLoginStudent
    put("std", std)
    val profiles = stdElectionService.getProfiles(std)
    CollectionUtils.filter(profiles, new Predicate() {

      def evaluate(`object`: AnyRef): Boolean = {
        val profile = `object`.asInstanceOf[ElectionProfile]
        if (Collections.isEmpty(profile.getProjects)) {
          return false
        }
        for (p <- profile.getProjects if p != profile.getProject) {
          return false
        }
        return true
      }
    })
    if (profiles.isEmpty) {
      return forward("notReady")
    }
    put("currentTime", System.currentTimeMillis())
    put("profiles", profiles)
    forward()
  }

  protected def generalCheck(profile: ElectionProfile, state: ElectState, std: SimpleStd): List[Message] = {
    if (!profile.isTimeSuitable) {
      val results = Collections.newBuffer[Any]
      results.add(new ElectMessage("不在选课时间内", ElectRuleType.GENERAL, false, null))
      results
    } else {
      val context = new ElectionCourseContext(new StudentBean(std.id), state)
      stdElectionService.generalCheck(profile, context)
    }
  }

  def defaultPage(): String = {
    val profileId = getLong("electionProfile.id")
    if (null == profileId) {
      return redirect("innerIndex")
    }
    val profile = entityDao.get(classOf[ElectionProfileBean], profileId)
    if (null == profile) {
      return forwardError("没有开放的选课轮次")
    }
    var state = ActionContext.getContext.getSession.get("electState" + profileId).asInstanceOf[ElectState]
    val std = getLoginStudent
    val builder = OqlBuilder.from(classOf[CourseTake].getName + " courseTake")
    builder.where("courseTake.std.id=:stdId", std.id)
    builder.where("courseTake.lesson.semester =:semester", profile.getSemester)
    val courseTakes = entityDao.search(builder)
    if (null == state || state.getProfileId != profileId) {
      val query = OqlBuilder.from(classOf[StudentProgram], "sp")
      query.where("sp.std=:std", std)
      val stdProgram = entityDao.uniqueResult(query)
      if (stdProgram == null) {
        val results = Collections.newBuffer[Any]
        results.add(new ElectMessage("学生未绑定计划", ElectRuleType.GENERAL, false, null))
        put("ELECTION", ElectRuleType.ELECTION)
        put("WITHDRAW", ElectRuleType.WITHDRAW)
        put("GENERAL", ElectRuleType.GENERAL)
        put("messages", results)
        return "electResult"
      }
      state = new ElectState(std, stdProgram, profile, null)
      for (config <- profile.getElectConfigs) {
        config.getParams.size
        config.getRule.getParams.size
      }
      for (config <- profile.getWithdrawConfigs) {
        config.getParams.size
        config.getRule.getParams.size
      }
      val messages = generalCheck(profile, state, state.getStd)
      if (!messages.isEmpty) {
        put("ELECTION", ElectRuleType.ELECTION)
        put("WITHDRAW", ElectRuleType.WITHDRAW)
        put("GENERAL", ElectRuleType.GENERAL)
        put("messages", messages)
        return "electResult"
      }
      val plan = coursePlanProvider.getCoursePlan(stdProgram)
      stdElectionService.prepare(profile, new PrepareContext(profile, state, std, courseTakes, plan))
      ActionContext.getContext.getSession.put("electState" + profileId, state)
    } else {
      val messages = generalCheck(profile, state, state.getStd)
      if (!messages.isEmpty) {
        put("ELECTION", ElectRuleType.ELECTION)
        put("WITHDRAW", ElectRuleType.WITHDRAW)
        put("GENERAL", ElectRuleType.GENERAL)
        put("messages", messages)
        return "electResult"
      }
    }
    if (state.getElectableLessonIds.isEmpty) {
      setLessons(state, profile, courseTakes)
    }
    put("electableIds", state.getElectableLessonIds)
    put("lessons", new SinglePage[Any](1, 20, 0, Collections.newBuffer[Any]))
    val project = new ProjectBean(state.getStd.getProjectId)
    var campus: Campus = null
    if (null != state.getStd.getCampusId) campus = new CampusBean(state.getStd.getCampusId)
    val units = Collections.newBuffer[Any](timeSettingService.getClosestTimeSetting(project, profile.getSemester, 
      campus)
      .getDefaultUnits
      .values)
    Collections.sort(units, new PropertyComparator("startTime"))
    put("units", units)
    put("weekDays", WeekDays.All)
    val style = CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY.toString).asInstanceOf[String])
    put("profileId", profileId)
    put("tableStyle", style)
    put("currentTime", new Date())
    val retakeLessonIds = Collections.newSet[Any]
    for (take <- courseTakes if take.getCourseTakeType.isRetake) retakeLessonIds.add(take.getLesson.id)
    put("retakeLessonIds", retakeLessonIds)
    val takeLessons = Collections.newBuffer[Any]
    for (take <- courseTakes if !profile.getElectableLessons.contains(take.getLesson.id)) {
      takeLessons.add(take.getLesson)
    }
    put("takedLessons", takeLessons)
    put("_profile", profile)
    put("std", std)
    put("electState", state)
    state.getElectedCourseIds.clear()
    for (take <- courseTakes) {
      state.getElectedCourseIds.put(take.getLesson.getCourse.id, take.getLesson.id)
    }
    forward("default")
  }

  def test(): String = {
    val profileId = getLong("profileId")
    if (profileId == null) {
      return forwardError("缺少参数ID")
    }
    val state = ActionContext.getContext.getSession.get("electState" + profileId).asInstanceOf[ElectState]
    val lessonId = getLong("lesson.id")
    val lessonNo = get("lesson.no")
    var lesson: Lesson = null
    if (null != lessonId) {
      lesson = entityDao.get(classOf[Lesson], lessonId)
    }
    if (null == lesson) {
      if (Strings.isNotBlank(lessonNo)) {
        val query = OqlBuilder.from(classOf[Lesson], "lesson").where("lesson.semester.id = :semesterId", 
          state.getSemesterId)
          .where("lesson.no = :no", lessonNo)
        lesson = entityDao.uniqueResult(query)
      }
    }
    val std = getLoginStudent
    var context = new ElectionCourseContext(std, state)
    val profile = state.getProfile(entityDao)
    context.setOp(ElectRuleType.ELECTION)
    context.setCourseTake(CourseTakeHelper.genCourseTake(lesson, std, getNormalTakeType, getSelfMode, 
      profile.getTurn, new Date()))
    if (null != lesson) {
      context = stdElectionService.test(context)
      put("electContext", context)
      return forward()
    }
    forwardError("请输入课程序号或者任务主键")
  }

  def searchHisCourse(): String = {
    val profileId = getLong("profileId")
    val state = ActionContext.getContext.getSession.get("electState" + profileId).asInstanceOf[ElectState]
    var courses = Collections.newBuffer[Any]
    if (null != state) {
      courses = entityDao.get(classOf[Course], state.getHisCourses.keySet)
      var orderBy = get(Order.ORDER_STR)
      if (null == orderBy) {
        orderBy = " asc"
      } else if (orderBy.startsWith("course.passed")) {
        orderBy.replace("course.passed", "")
      }
      val order = if (orderBy.contains(" asc")) 1 else -1
      Collections.sort(courses, new Comparator[Course]() {

        def compare(o1: Course, o2: Course): Int = {
          return state.getHisCourses.get(o1.id).compareTo(state.getHisCourses.get(o2.id)) * 
            order
        }
      })
    }
    put("courses", courses)
    val openedCourseIds = Collections.newBuffer[Any]
    put("openedCourseIds", openedCourseIds)
    val courseIdToOrisAndSubs = Collections.newMap[Any]
    var groupIndex = 0
    for (esub <- state.getCourseSubstitutions) {
      val oriIds = esub.getOrigins
      val subs = entityDao.get(classOf[Course], esub.getSubstitutes)
      var tuple: Triple[Set[Long], List[List[Course]], Integer] = null
      for (courseId <- courseIdToOrisAndSubs.keySet if oriIds == courseIdToOrisAndSubs.get(courseId)._1) {
        tuple = courseIdToOrisAndSubs.get(courseId.toString)
        //break
      }
      if (tuple == null) {
        tuple = new Triple[Set[Long], List[List[Course]], Integer](oriIds, new ArrayList[List[Course]](), 
          groupIndex)
        groupIndex += 1
      }
      tuple._2.add(subs)
      for (oriId <- oriIds) {
        courseIdToOrisAndSubs.put(oriId.toString, tuple)
      }
    }
    Collections.sort(courses, new Comparator[Course]() {

      def compare(lhs: Course, rhs: Course): Int = {
        var lhsWeight = java.lang.Integer.MAX_VALUE
        if (courseIdToOrisAndSubs.get(lhs.id) != null) {
          lhsWeight = courseIdToOrisAndSubs.get(lhs.id)._3
        }
        val rhsWeight = java.lang.Integer.MAX_VALUE
        if (courseIdToOrisAndSubs.get(rhs.id) != null) {
          lhsWeight = courseIdToOrisAndSubs.get(rhs.id)._3
        }
        return lhsWeight - rhsWeight
      }
    })
    put("electState", state)
    put("courseIdToOrisAndSubs", courseIdToOrisAndSubs)
    forward()
  }

  def data(): String = {
    val profileId = getLong("profileId")
    val state = ActionContext.getContext.getSession.get("electState" + profileId).asInstanceOf[ElectState]
    val postfix = "_" + state.getStd.id
    var responseETag = electionProfileService.getLastUpdateTime(profileId) + 
      postfix
    val request = getRequest
    val response = getResponse
    val requestETag = request.getHeader(HEADER_IF_NONE_MATCH)
    if (responseETag == requestETag) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
    } else {
      val s = electionProfileService.getData(profileId, state.getElectableLessonIds)
      if (("null" + postfix) == responseETag) {
        responseETag = electionProfileService.getLastUpdateTime(profileId) + 
          postfix
      }
      response.setHeader(HEADER_ETAG, responseETag)
      var pw: PrintWriter = null
      response.setContentType("text/javascript;charset=UTF-8")
      try {
        pw = response.getWriter
        pw.write(s)
        pw.flush()
        pw.close()
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
    null
  }

  protected def setLessons(state: ElectState, profile: ElectionProfile, takes: List[CourseTake]) {
    if (profile.getElectableLessons.isEmpty) return
    val courses = Collections.newSet[Any]
    for (lesson <- electionProfileService.getLessons(profile.id) if stdElectionService.isElectable(lesson, 
      state)) {
      state.getElectableLessonIds.add(lesson.id)
      courses.add(lesson.getCourse)
    }
    for (take <- takes) {
      val electLesson = take.getLesson
      if (!state.getElectableLessonIds.contains(electLesson.id)) {
        state.getElectableLessonIds.add(electLesson.id)
        courses.add(electLesson.getCourse)
      }
    }
    if (null != state.getCoursePlan) {
      state.getCoursePlan.filter(courses)
    }
  }

  def batchOperator(): String = {
    val profileId = getLong("profileId")
    val state = ActionContext.getContext.getSession.get("electState" + profileId).asInstanceOf[ElectState]
    if (null == state) {
      return prompt()
    }
    val now = new Date()
    val messages = Collections.newBuffer[Any]
    val std = new StudentBean(state.getStd.id)
    val operatorMap = populateOperatorMap(std, state)
    val profile = state.getProfile(entityDao)
    if (!profile.isTimeSuitable) {
      messages.add(new ElectMessage("当前未开放", ElectRuleType.GENERAL, false, null))
    } else {
      std.setCode(state.getStd.getCode)
      std.setName(state.getStd.getName)
      val op = get("operator" + 1)
      if (true != state.getParams.get("batchOperator") && null != op) {
        messages.add(new ElectMessage("操作失败,当前禁止批量选课", ElectRuleType.GENERAL, false, null))
      } else {
        val electLessonIds = operatorMap.get(ElectRuleType.ELECTION)
        var electLessons = Collections.emptyList()
        if (!electLessonIds.isEmpty) {
          if (!profile.isElectionTimeSuitable(now)) {
            messages.add(new ElectMessage("当前选课不开放", ElectRuleType.GENERAL, false, null))
          } else {
            electLessons = entityDao.get(classOf[Lesson], electLessonIds)
          }
        }
        val withdrawLessonIds = operatorMap.get(ElectRuleType.WITHDRAW)
        var withdrawCourseTakes = Collections.emptyList()
        if (!withdrawLessonIds.isEmpty) {
          if (!profile.isWithdrawTimeSuitable(now)) {
            messages.add(new ElectMessage("当前退课不开放", ElectRuleType.GENERAL, false, null))
          } else {
            withdrawCourseTakes = entityDao.get(classOf[CourseTake], Array("lesson.id", "std.id"), Array(withdrawLessonIds, std.id))
          }
        }
        val contextsMap = buildContextsMap(withdrawCourseTakes, electLessons, std, state, profile)
        val resultMessages = stdElectionService.batchOperator(contextsMap)
        for (msgs <- resultMessages) {
          messages.addAll(msgs)
        }
        if (messages.isEmpty) {
          messages.add(new ElectMessage("操作失败,请联系管理员", ElectRuleType.GENERAL, false, null))
        }
      }
    }
    put("electLessonIds", operatorMap.get(ElectRuleType.ELECTION))
    put("withdrawLessonIds", operatorMap.get(ElectRuleType.WITHDRAW))
    put("ELECTION", ElectRuleType.ELECTION)
    put("WITHDRAW", ElectRuleType.WITHDRAW)
    put("GENERAL", ElectRuleType.GENERAL)
    put("messages", messages)
    put("electState", state)
    forward("electResult")
  }

  protected def buildContextsMap(withdrawCourseTakes: Iterable[CourseTake], 
      electLessons: Iterable[Lesson], 
      std: Student, 
      state: ElectState, 
      profile: ElectionProfile): Map[ElectRuleType, List[ElectionCourseContext]] = {
    val contextsMap = Collections.newMap[Any]
    contextsMap.put(ElectRuleType.ELECTION, new ArrayList[ElectionCourseContext]())
    contextsMap.put(ElectRuleType.WITHDRAW, new ArrayList[ElectionCourseContext]())
    val turn = profile.getTurn
    for (courseTake <- withdrawCourseTakes) {
      val context = new ElectionCourseContext(std, state)
      context.setOp(ElectRuleType.WITHDRAW)
      context.setCourseTake(courseTake)
      contextsMap.get(ElectRuleType.WITHDRAW).add(context)
    }
    for (lesson <- electLessons) {
      val context = new ElectionCourseContext(std, state)
      context.setOp(ElectRuleType.ELECTION)
      context.setState(state)
      context.setStudent(std)
      val take = CourseTakeHelper.genCourseTake(lesson, std, getNormalTakeType, getSelfMode, turn, new Date())
      context.setCourseTake(take)
      context.getParams.put(Params.CONFLICT_LESSONS.toString, electLessons)
      contextsMap.get(ElectRuleType.ELECTION).add(context)
    }
    contextsMap
  }

  protected def populateOperatorMap(std: Student, state: ElectState): Map[ElectRuleType, Set[Long]] = {
    var op = get("operator" + 0)
    val operatorMap = Collections.newMap[Any]
    operatorMap.put(ElectRuleType.ELECTION, new HashSet[Long]())
    operatorMap.put(ElectRuleType.WITHDRAW, new HashSet[Long]())
    var i = 0
    while (null != op) {
      val vals = op.split(":")
      val lessonId = java.lang.Long.parseLong(vals(0))
      val `type` = if ("true" == vals(1)) ElectRuleType.ELECTION else ElectRuleType.WITHDRAW
      operatorMap.get(`type`).add(lessonId)
      op = get("operator" + (i))
    }
    operatorMap
  }

  def failCourseStat(): String = {
    val std = getLoginStudent
    val cheatCourseBuilder = OqlBuilder.from(classOf[CourseGrade].getName + " courseGrade")
    cheatCourseBuilder.select("select distinct courseGrade.course")
    cheatCourseBuilder.join("courseGrade.examGrades", "examGrade")
    cheatCourseBuilder.where("courseGrade.std=:std", std)
    cheatCourseBuilder.where("examGrade.examStatus.name=:violationName", "违纪")
    val cheatedCourses = Collections.newHashSet(entityDao.search(cheatCourseBuilder))
    val profileId = getLong("profileId")
    val state = ActionContext.getContext.getSession.get("electState" + profileId).asInstanceOf[ElectState]
    if (null == state) {
      return forward("notReady")
    }
    val profile = state.getProfile(entityDao)
    if (null == profile) {
      return forward("notReady")
    }
    val auditResult = planAuditService.audit(std, new PlanAuditContext())
    val failCourses = Collections.newBuffer[Any]
    if (null != auditResult) {
      for (groupAuditResult <- auditResult.getGroupResults if !groupAuditResult.getAuditStat.passed; 
           courseAuditResult <- groupAuditResult.getCourseResults if !groupAuditResult.getAuditStat.passed && null == courseAuditResult.getScores
          if !cheatedCourses.contains(courseAuditResult.getCourse)) {
        groupAuditResult.getCourseResults.add(courseAuditResult)
        failCourses.add(courseAuditResult.getCourse)
      }
    }
    val courseApplyMap = Collections.newMap[Any]
    buildApplyStat(failCourses, std.id, profile.getSemester.id)
    put("failCourses", failCourses)
    put("semesterId", get("semesterId"))
    put("courseApplyMap", courseApplyMap)
    put("cheatedCourses", cheatedCourses)
    put("auditResult", auditResult)
    forward()
  }

  def queryStdCount(): String = {
    val request = getRequest
    val response = getResponse
    val profileId = getLong("profileId")
    var responseETag = electionProfileService.getLastQueryStdCountTime(profileId)
    val requestETag = request.getHeader(HEADER_IF_NONE_MATCH)
    if (responseETag == requestETag) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
    } else {
      val s = electionProfileService.queryStdCount(profileId)
      if ("null" == responseETag) {
        responseETag = electionProfileService.getLastQueryStdCountTime(profileId)
      }
      response.setHeader(HEADER_ETAG, responseETag)
      var pw: PrintWriter = null
      response.setContentType("text/javascript;charset=UTF-8")
      try {
        pw = response.getWriter
        pw.write(s)
        pw.flush()
        pw.close()
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
    null
  }

  def getGroupAuditResults(std: Student): List[GroupAuditResult] = {
    val planAuditResult = planAuditService.audit(std, new PlanAuditContext())
    if (null == planAuditResult) {
      val results = Collections.newBuffer[Any]
      return results
    }
    planAuditResult.getGroupResults
  }

  protected def calcCredit(auditResult: PlanAuditResult, 
      semester: Semester, 
      plan: CoursePlan, 
      term: Int, 
      termCalculator: TermCalculator): Map[Integer, Float] = {
    if (null == auditResult) return null
    val restrictionCredit = Collections.newMap[Any]
    for (courseGroup <- plan.getGroups) {
      val creditPerTerms = courseGroup.getTermCredits.substring(1, courseGroup.getTermCredits.length - 1)
        .split(",")
      for (i <- 0 until creditPerTerms.length if (i + 1) == term) {
        restrictionCredit.put(courseGroup.getCourseType.id, java.lang.Float.parseFloat(creditPerTerms(i).toString))
      }
    }
    restrictionCredit
  }

  def getStdElectCredit(std: Student, 
      semester: Semester, 
      auditResult: PlanAuditResult, 
      plan: CoursePlan): Map[Integer, Double] = {
    val builder = OqlBuilder.from(classOf[CourseTake], "take")
    builder.where("take.courseTakeType.id =:compulsoryId ", CourseTakeType.NORMAL)
    builder.where("take.lesson.semester=:semester", semester)
    builder.where("take.std=:std", std)
    val takes = entityDao.search(builder)
    val courseTypes = getStdCourseTypes(auditResult, plan)
    val stdCreditMap = Collections.newMap[Any]
    var credit = 0D
    for (take <- takes) {
      val lesson = take.getLesson
      var `type` = courseTypes.get(lesson.getCourse.id)
      val courseGroup = plan.getGroup(lesson.getCourseType)
      if (null != courseGroup && !courseGroup.isCompulsory) {
        `type` = lesson.getCourseType
      }
      if (null != `type`) {
        var credits = stdCreditMap.get(`type`.id)
        if (null == credits) {
          credits = 0.0
        }
        credits += lesson.getCourse.getCredits
        stdCreditMap.put(`type`.id, credits)
      } else if (courseGroup.isInstanceOf[SharePlanCourseGroup]) {
        credit += lesson.getCourse.getCredits
        stdCreditMap.put(courseGroup.getCourseType.id, credit)
      }
    }
    stdCreditMap
  }

  protected def getCourseTypeCredit(semester: Semester, std: Student, state: ElectState): Map[Long, Float] = {
    if (null == state.getParams.get("COURSE_TYPE_CREDIT_MAP")) {
      val builder = OqlBuilder.from(classOf[CourseTypeCreditConstraint], "courseTypeCreditConstraint")
      builder.where("courseTypeCreditConstraint.semester=:semester", semester)
      builder.where("courseTypeCreditConstraint.grade=:grade", std.grade)
      builder.where("courseTypeCreditConstraint.education=:education", std.education)
      val courseTypeCreditConstraints = entityDao.search(builder)
      val courseTypeCreditMap = Collections.newMap[Any]
      for (courseTypeCreditConstraint <- courseTypeCreditConstraints) {
        courseTypeCreditMap.put(courseTypeCreditConstraint.getCourseType.id, courseTypeCreditConstraint.getLimitCredit)
      }
      state.getParams.put("COURSE_TYPE_CREDIT_MAP", courseTypeCreditMap)
    }
    state.getParams.get("COURSE_TYPE_CREDIT_MAP").asInstanceOf[Map[Long, Float]]
  }

  def getStdCourseTypes(auditResult: PlanAuditResult, plan: CoursePlan): Map[Long, CourseType] = {
    val courseTypeMap = Collections.newMap[Any]
    if (null != auditResult && null != plan) {
      for (courseGroup <- plan.getGroups; planCourse <- courseGroup.getPlanCourses) {
        courseTypeMap.put(planCourse.getCourse.id, courseGroup.getCourseType)
      }
    }
    courseTypeMap
  }

  def prompt(): String = {
    request()
    val profileId = getLong("profileId")
    val state = ActionContext.getContext.getSession.get("electState" + profileId).asInstanceOf[ElectState]
    if (null == state || state.getStd.getCode != getUser) {
      val std = getLoginStudent
      entityDao.initialize(std.getAdminclass)
      val profiles = stdElectionService.getProfiles(std)
      if (profiles.isEmpty) {
        return forward("notReady")
      }
      try {
        val plan = coursePlanProvider.getCoursePlan(std)
        val calc = new TermCalculator(semesterService, state.getProfile(entityDao).getSemester)
        val term = calc.getTerm(std.grade, true)
        if (plan != null) {
          for (group <- plan.getGroups) {
            val credits = Strings.split(group.getTermCredits, ",")
            if (credits(term - 1) != "0") {
              entityDao.initialize(group.getCourseType)
            }
          }
        }
      } catch {
        case e: Exception => logger.debug("maybe somebody with no plan,or on campusTime")
      }
      val lessons = Collections.newBuffer[Any]
      var electedStat = 0
      for (lesson <- lessons) {
        electedStat += lesson.getCourse.getCredits
        state.getElectedCourseIds.put(lesson.getCourse.id, lesson.id)
      }
      if (electedStat != state.getElectedCredit) {
        state.setElectedCredit(electedStat)
      }
      ActionContext.getContext.getSession.put("electState" + profileId, state)
    }
    put("electState", state)
    put("now", new java.util.Date())
    put("entityDao", entityDao)
    forward("prompt")
  }

  def request(): String = {
    val std = getLoginStudent
    val profileId = getLong("profileId")
    val state = ActionContext.getContext.getSession.get("electState" + profileId).asInstanceOf[ElectState]
    var semester: Semester = null
    if (null == state || null == state.getProfile(entityDao)) {
      return forward("notReady")
    } else {
      semester = state.getProfile(entityDao).getSemester
    }
    val builder = OqlBuilder.from(classOf[StdApplyLog], "log")
    builder.where("log.stdId=:stdId", std.id)
    builder.where("log.semesterId=:semesterId", semester.id)
    val logs = entityDao.search(builder)
    put("logs", logs)
    forward()
  }

  private def buildApplyStat(courses: Iterable[Course], stdId: java.lang.Long, semesterId: java.lang.Integer): Map[String, String] = {
    val applyQry = OqlBuilder.from(classOf[StdApplyLog], "log")
    applyQry.where("log.stdId=:stdId", stdId)
    applyQry.where("log.semesterId=:semesterId", semesterId)
    val logs = entityDao.search(applyQry)
    val logMap = Collections.newMap[Any]
    for (log <- logs) {
      val exists = logMap.get(log.getCourseCode)
      if (null == exists) {
        logMap.put(log.getCourseCode, log)
      } else {
        if (exists.getApplyOn.before(log.getApplyOn)) {
          logMap.put(log.getCourseCode, log)
        }
      }
    }
    val courseTakeQuery = OqlBuilder.from(classOf[CourseTake].getName + " take")
    courseTakeQuery.select("select take.task.course.id")
    courseTakeQuery.where("take.std.id=:stdId", stdId)
    val courseApplyMap = Collections.newMap[Any]
    if (courses.size > 0) {
      courseTakeQuery.where("take.task.course in (:courses)", courses)
    } else {
      return courseApplyMap
    }
    courseTakeQuery.where("take.task.semester.id =:semesterId", semesterId)
    val takeCourseIds = Collections.newHashSet(entityDao.search(courseTakeQuery))
    for (course <- courses) {
      if (takeCourseIds.contains(course.id)) {
        courseApplyMap.put(course.id.toString, "已选")
      } else {
        val log = logMap.get(course.getCode)
        if (null == log) {
          //continue
        }
        courseApplyMap.put(course.id.toString, log.getApplyType.getName)
      }
    }
    courseApplyMap
  }

}
