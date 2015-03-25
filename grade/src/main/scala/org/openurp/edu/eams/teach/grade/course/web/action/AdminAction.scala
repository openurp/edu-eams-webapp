package org.openurp.edu.eams.teach.grade.course.web.action
import java.util.Date



import org.apache.commons.collections.CollectionUtils
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.bean.transformers.PropertyTransformer
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.struts2.convention.route.Action
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.StudentService
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.grade.course.service.GradeInputSwitchService
import org.openurp.edu.eams.teach.grade.course.service.GradeTypePolicy
import org.openurp.edu.eams.teach.grade.course.service.MakeupStdStrategy
import org.openurp.edu.eams.teach.grade.course.service.MarkStyleStrategy
import org.openurp.edu.eams.teach.grade.course.web.helper.CourseGradeHelper
import org.openurp.edu.eams.teach.grade.course.web.helper.TeachClassGradeHelper
import org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch
import org.openurp.edu.eams.teach.grade.lesson.service.LessonGradeService
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.model.GradeRateConfig
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.eams.teach.grade.service.GradeCourseTypeProvider
import org.openurp.edu.eams.teach.grade.service.GradeRateService
import org.openurp.edu.eams.teach.grade.service.event.CourseGradeSubmitEvent
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.grade.ExamGrade
import org.openurp.edu.teach.grade.model.ExamGradeState
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseGradeBean
import org.openurp.edu.eams.teach.lesson.model.CourseGradeStateBean
import org.openurp.edu.eams.teach.lesson.model.ExamGradeBean
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategyFactory
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class AdminAction extends SemesterSupportAction {

  var lessonFilterStrategyFactory: LessonFilterStrategyFactory = _

  var makeupStdStrategy: MakeupStdStrategy = _

  var gradeInputSwitchService: GradeInputSwitchService = _

  var lessonService: LessonService = _

  var courseGradeService: CourseGradeService = _

  var lessonGradeService: LessonGradeService = _

  var courseGradeHelper: CourseGradeHelper = _

  var calculator: CourseGradeCalculator = _

  var gradeRateService: GradeRateService = _

  var teachClassGradeHelper: TeachClassGradeHelper = _

  var gradeTypePolicy: GradeTypePolicy = _

  var markStyleStrategy: MarkStyleStrategy = _

  var settings: CourseGradeSettings = _

  var studentService: StudentService = _

  var gradeCourseTypeProvider: GradeCourseTypeProvider = _

  protected def checkState(): String = {
    val lessonId = getLong("lessonId")
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    if (null == lesson) {
      return forwardError("error.parameters.illegal")
    }
    val gis = getGradeInputSwitch(lesson)
    put("gradeInputSwitch", gis)
    if (!gis.checkOpen(new Date())) {
      return forward("../teacher/cannotInput")
    }
    val gradeState = getOrCreateState(lesson)
    put("gradeState", gradeState)
    for (gradeType <- getGradeTypes(gradeState) if null != getState(gradeType) && Grade.Status.NEW != getState(gradeType).getStatus && 
      getState(gradeType).getStatus > Grade.Status.CONFIRMED) {
      return redirect(new Action(classOf[AdminAction], "inputTask", "&lessonId=" + lesson.id), "error.grade.modifyPublished")
    }
    null
  }

  protected def getState(gradeType: GradeType): ExamGradeState = {
    val gradeState = getGradeState
    var gradeTypeState = gradeState.getState(gradeType)
    var precision = getInt("precision")
    if (null == precision) precision = new java.lang.Integer(0)
    if (null == gradeTypeState) {
      gradeTypeState = Model.newInstance(classOf[ExamGradeState])
      gradeTypeState.setGradeType(gradeType)
      gradeTypeState.setStatus(Grade.Status.NEW)
      gradeTypeState.setGradeState(getGradeState)
      gradeTypeState.setPrecision(precision)
      gradeTypeState.setInputedAt(new Date())
      gradeTypeState.setScoreMarkStyle(Model.newInstance(classOf[ScoreMarkStyle], gradeState.getScoreMarkStyle.id))
      gradeState.getStates.add(gradeTypeState)
    }
    gradeTypeState
  }

  protected def getStates(): List[ExamGradeState] = {
    val gradeState = getGradeState
    val myStats = CollectUtils.newArrayList()
    for (gradeType <- getGradeTypes(gradeState)) {
      var examGradeState = gradeState.getState(gradeType)
      if (null == examGradeState) {
        examGradeState = Model.newInstance(classOf[ExamGradeState])
        examGradeState.setGradeType(gradeType)
        examGradeState.setGradeState(gradeState)
        entityDao.saveOrUpdate(gradeState)
      }
      myStats.add(examGradeState)
    }
    myStats
  }

  protected def getGradeTypes(gradeState: CourseGradeState): List[GradeType] = {
    var gradeTypes = getAttribute("gradeTypes").asInstanceOf[List[GradeType]]
    if (null == gradeTypes) {
      gradeTypes = CollectUtils.newArrayList()
      var gradeTypeIds = Array()
      val gradeTypeIdSeq = get("gradeTypeIds")
      if (Strings.isNotBlank(gradeTypeIdSeq)) gradeTypeIds = Strings.splitToInt(gradeTypeIdSeq)
      val gradeInputSwitch = getAttribute("gradeInputSwitch").asInstanceOf[GradeInputSwitch]
      for (typeId <- gradeTypeIds) {
        val gradeType = baseCodeService.getCode(classOf[GradeType], typeId).asInstanceOf[GradeType]
        if (gradeInputSwitch.getTypes.contains(gradeType)) gradeTypes.add(gradeType)
      }
    }
    gradeTypes
  }

  protected def removeZeroPercentTypes(gradeState: CourseGradeState, gradeTypes: List[GradeType]) {
    if (CollectUtils.isEmpty(gradeTypes)) {
      return
    }
    val zeroPercentTypes = CollectUtils.newArrayList()
    for (`type` <- gradeTypes) {
      val egState = gradeState.getState(`type`)
      if (null != egState.getPercent) {
        if (egState.getPercent == 0F) {
          zeroPercentTypes.add(egState.gradeType)
        }
      }
    }
    for (`type` <- zeroPercentTypes) {
      gradeTypes.remove(`type`)
      val egState = gradeState.getState(`type`)
      if (null != egState) gradeState.getStates.remove(egState)
    }
  }

  protected def getCourseTakes(lesson: Lesson): List[CourseTake] = {
    val takes = CollectUtils.newArrayList(lesson.getTeachClass.getCourseTakes)
    Collections.sort(takes, new PropertyComparator("std.code"))
    takes
  }

  def index(): String = {
    val teacher = getLoginTeacher
    if (teacher == null) {
      return forwardError("没有权限")
    }
    setSemesterDataRealm(hasStdTypeCollege)
    put("projects", getProjects)
    val semesters = gradeInputSwitchService.getOpenedSemesters(getProject)
    if (!semesters.isEmpty) put("semester", semesters.get(0))
    forward()
  }

  def taskList(): String = {
    val teacher = getLoginTeacher
    val user = entityDao.get(classOf[User], getUserId)
    val semesterId = getInt("lesson.semester.id")
    if (semesterId == null) {
      return forwardError("error.teacher.noTask")
    }
    val semester = entityDao.get(classOf[Semester], semesterId)
    val lessons = lessonService.getLessonByCategory(teacher.id, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.TEACHER), 
      semester)
    val builder = OqlBuilder.from(classOf[CourseGradeState], "courseGradeState")
    builder.where("courseGradeState.extraInputer = :user", user)
    if (!lessons.isEmpty) {
      builder.where("courseGradeState.lesson not in (:lessons)", lessons)
    }
    builder.where("courseGradeState.lesson.semester=:semester", semester)
    builder.select("courseGradeState.lesson")
    lessons.addAll(entityDao.search(builder).asInstanceOf[List[Lesson]])
    put("lessons", lessons)
    val gradeStates = CollectUtils.newHashMap()
    for (lesson <- lessons) {
      val state = courseGradeService.getState(lesson)
      if (state != null) gradeStates.put(lesson.id, state)
    }
    put("makeupTakeCounts", makeupStdStrategy.getCourseTakeCounts(lessons))
    put("gradeStates", gradeStates)
    put("gradeInputSwitch", getGradeInputSwitch(getProject, semester))
    put("GA", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.GA_ID))
    put("MAKEUP", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.MAKEUP_ID))
    put("DELAY", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.DELAY_ID))
    forward()
  }

  protected def buildSomeParams(lesson: Lesson, putScomeParams: Set[String]) {
    val state = courseGradeService.getState(lesson)
    if (null == putScomeParams) {
      return
    }
    if (putScomeParams.contains("gradeConverterConfig")) {
      val query = OqlBuilder.from(classOf[GradeRateConfig], "config")
      query.where("config.scoreMarkStyle = :markStyle", state.getScoreMarkStyle)
      val configs = entityDao.search(query)
      if (CollectUtils.isNotEmpty(configs)) {
        put("gradeConverterConfig", configs.get(0))
      }
    }
    if (putScomeParams.contains("markStyles")) {
      put("markStyles", gradeRateService.getMarkStyles(getProject))
    }
    if (putScomeParams.contains("gradeRateConfiges")) {
      put("gradeRateConfiges", entityDao.getAll(classOf[GradeRateConfig]))
    }
    if (putScomeParams.contains("userCategoryId")) {
      put("userCategoryId", getUserCategoryId)
    }
    if (putScomeParams.contains("PERCENT")) {
      put("PERCENT", baseCodeService.getCode(classOf[ScoreMarkStyle], ScoreMarkStyle.PERCENT))
    }
    if (putScomeParams.contains("RANK_EN")) {
      put("RANK_EN", baseCodeService.getCode(classOf[ScoreMarkStyle], ScoreMarkStyle.RANK_EN))
    }
    if (putScomeParams.contains("GA")) {
      put("GA", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.GA_ID))
    }
    if (putScomeParams.contains("MIDDLE")) {
      put("MIDDLE", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.MIDDLE_ID))
    }
    if (putScomeParams.contains("USUAL")) {
      put("USUAL", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.USUAL_ID))
    }
    if (putScomeParams.contains("END")) {
      put("END", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.END_ID))
    }
    if (putScomeParams.contains("MAKEUP")) {
      put("MAKEUP", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.MAKEUP_ID))
    }
    if (putScomeParams.contains("RESTUDY")) {
      put("RESTUDY", CourseTakeType.RESTUDY)
    }
    if (putScomeParams.contains("REEXAM")) {
      put("REEXAM", CourseTakeType.REEXAM)
    }
    if (putScomeParams.contains("NORMAL")) {
      put("NORMAL", ExamStatus.NORMAL)
    }
    if (putScomeParams.contains("ABSENT")) {
      put("ABSENT", ExamStatus.ABSENT)
    }
    if (putScomeParams.contains("CHEAT")) {
      put("CHEAT", ExamStatus.CHEAT)
    }
    if (putScomeParams.contains("DELAY")) {
      put("DELAY", ExamStatus.DELAY)
    }
    if (putScomeParams.contains("VIOLATION")) {
      put("VIOLATION", ExamStatus.VIOLATION)
    }
    if (putScomeParams.contains("NEW")) {
      put("NEW", Grade.Status.NEW)
    }
    if (putScomeParams.contains("CONFIRMED")) {
      put("CONFIRMED", Grade.Status.CONFIRMED)
    }
    if (putScomeParams.contains("examStatuses")) {
      val query = OqlBuilder.from(classOf[ExamStatus], "examStatus")
      query.where("examStatus.effectiveAt <= :now and (examStatus.invalidAt is null or examStatus.invalidAt >= :now)", 
        new Date())
      query.orderBy("examStatus.code")
      put("examStatuses", entityDao.search(query))
    }
    if (putScomeParams.contains("isTeacher")) {
      put("isTeacher", false)
    }
    if (putScomeParams.contains("gradeRateConfigs")) {
      val gradeRateConfigs = CollectUtils.newHashMap()
      val gradeTypes = getGradeTypes(state)
      for (gradeType <- gradeTypes) {
        if (GradeTypeConstants.GA_ID == gradeType.id) {
          //continue
        }
        val examGradeState = state.getState(gradeType)
        val scoreMarkStyle = entityDao.get(classOf[ScoreMarkStyle], examGradeState.getScoreMarkStyle.id)
        if (!scoreMarkStyle.isNumStyle) {
          if (!gradeRateConfigs.containsKey(scoreMarkStyle)) {
            gradeRateConfigs.put(scoreMarkStyle, gradeRateService.getConfig(getProject, scoreMarkStyle))
          }
        }
        put("gradeRateConfigs", gradeRateConfigs)
      }
    }
  }

  protected def buildGradeConfig(lesson: Lesson, gradeTypes: List[GradeType]) {
    val gradeState = getGradeState
    val markStyles = CollectUtils.newHashMap()
    val examTypes = CollectUtils.newHashSet()
    for (gradeType <- gradeTypes) {
      val gradeTypeState = gradeState.getState(gradeType)
      if (null == gradeTypeState) {
        //continue
      }
      var markStyle = gradeTypeState.getScoreMarkStyle
      if (null == markStyle) markStyle = gradeState.getScoreMarkStyle
      if (null != gradeType.getExamType) examTypes.add(gradeType.getExamType)
    }
    val converterMap = CollectUtils.newHashMap()
    for (gradeTypeState <- gradeState.getStates) {
      markStyles.put(gradeTypeState.gradeType.id.toString, gradeTypeState.getScoreMarkStyle)
    }
    put("markStyles", markStyles)
    put("converterMap", converterMap)
    put("stdExamTypeMap", getStdExamTypeMap(lesson, examTypes))
  }

  protected def getStdExamTypeMap(lesson: Lesson, examTypes: Set[ExamType]): Map[String, ExamTake] = {
    if (CollectUtils.isEmpty(lesson.getTeachClass.getCourseTakes) || 
      examTypes.isEmpty) {
      return CollectUtils.newHashMap()
    }
    val query = OqlBuilder.from(classOf[ExamTake], "examTake").where("examTake.lesson=:lesson", lesson)
    if (CollectUtils.isNotEmpty(examTypes)) {
      query.where("examTake.examType in (:examTypes)", examTypes)
    }
    val stdExamTypeMap = CollectUtils.newHashMap()
    val examTakes = entityDao.search(query)
    for (examTake <- examTakes) {
      stdExamTypeMap.put(examTake.getStd.id + "_" + examTake.getExamType.id, examTake)
    }
    stdExamTypeMap
  }

  def inputTask(): String = {
    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
    val gradeInputSwitch = getGradeInputSwitch(lesson)
    put("gradeInputSwitch", gradeInputSwitch)
    put("gradeState", getOrCreateState(lesson))
    val putSomeParams = CollectUtils.newHashSet()
    putSomeParams.add("MAKEUP")
    putSomeParams.add("GA")
    putSomeParams.add("isTeacher")
    put("markStyles", gradeRateService.getMarkStyles(lesson.getProject))
    buildSomeParams(lesson, putSomeParams)
    put("DELAY_ID", GradeTypeConstants.DELAY_ID)
    val gaGradeTypes = settings.getSetting(getProject).getGaElementTypes
    val gaGradeTypeParams = CollectUtils.newArrayList()
    for (gradeType <- gaGradeTypes) {
      gradeType = entityDao.get(classOf[GradeType], gradeType.id)
      if (gradeInputSwitch.getTypes.contains(gradeType)) gaGradeTypeParams.add(gradeType)
    }
    put("gaGradeTypes", gaGradeTypeParams)
    put("lesson", lesson)
    forward()
  }

  def save(): String = {
    val result = checkState()
    if (null != result) {
      return result
    }
    val submit = !getBool("justSave")
    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
    val existGradeMap = getExistGradeMap(lesson)
    val setting = settings.getSetting(getProject)
    val isPublish = setting.isSubmitIsPublish
    if (submit) {
      val gradeState = courseGradeService.getState(lesson)
      val examGradeStates = gradeState.getStates
      val existGradeTypes = CollectUtils.collect(examGradeStates, new PropertyTransformer("gradeType"))
      if (!existGradeTypes.contains(Model.newInstance(classOf[GradeType], GradeTypeConstants.MAKEUP_ID)) && 
        !existGradeTypes.contains(Model.newInstance(classOf[GradeType], GradeTypeConstants.DELAY_ID))) {
        for (courseGrade <- existGradeMap.values; gradeType <- setting.getGaElementTypes) {
          val examGradeState = gradeState.getState(gradeType)
          val examGrade = courseGrade.getExamGrade(gradeType)
          if (examGradeState != null && null != examGrade) {
            if (examGradeState.getPercent == null || examGradeState.getPercent == 0) {
              courseGrade.getExamGrades.remove(examGrade)
            } else {
              examGrade.setPercent(null)
            }
          }
        }
      }
    }
    val inputedAt = new Date()
    val grades = CollectUtils.newArrayList()
    val status = if (submit) Grade.Status.CONFIRMED else Grade.Status.NEW
    val takes = getCourseTakes(lesson)
    for (take <- takes) {
      val grade = buildCourseGrade(existGradeMap.get(take.getStd), take, status, inputedAt)
      if (null != lesson.getExamMode) {
        grade.setExamMode(lesson.getExamMode)
      }
      if (null != grade) grades.add(grade)
    }
    if (submit) {
      updateGradeState(Grade.Status.CONFIRMED, inputedAt)
    } else {
      updateGradeState(Grade.Status.NEW, inputedAt)
    }
    val gradeState = getGradeState
    val operator = getUsername
    gradeState.setOperator(operator)
    val params = new StringBuilder("&lessonId=" + lesson.id)
    params.append("&gradeTypeIds=")
    val inputableGradeTypes = getGradeTypes(gradeState)
    for (gradeType <- inputableGradeTypes) {
      getState(gradeType).setOperator(operator)
      params.append(gradeType.id + ",")
    }
    entityDao.saveOrUpdate(grades, gradeState)
    if (submit) {
      if (isPublish) {
        val publishableGradeTypes = CollectionUtils.intersection(inputableGradeTypes, setting.getPublishableTypes).asInstanceOf[List[_]]
        var alreadyContainGA = false
        var alreadyContainFINAL = false
        for (publishGradeType <- publishableGradeTypes) {
          if (publishGradeType.id == GradeTypeConstants.FINAL_ID) {
            alreadyContainFINAL = true
          } else if (publishGradeType.id == GradeTypeConstants.GA_ID) {
            alreadyContainGA = true
          }
        }
        if (!alreadyContainGA) {
          publishableGradeTypes.add(Model.newInstance(classOf[GradeType], GradeTypeConstants.GA_ID))
        }
        if (!alreadyContainFINAL) {
          publishableGradeTypes.add(Model.newInstance(classOf[GradeType], GradeTypeConstants.FINAL_ID))
        }
        courseGradeService.publish(lesson.id + "", publishableGradeTypes.toArray(Array()), true)
      }
      publish(new CourseGradeSubmitEvent(gradeState))
    }
    params.deleteCharAt(params.length - 1)
    val toInputGradeTypeids = get("toInputGradeType.id")
    if (Strings.isNotEmpty(toInputGradeTypeids)) {
      params.append("&toInputGradeType.ids=" + get("toInputGradeType.id"))
      val toInputGradeTypeIds = Strings.splitToInt(toInputGradeTypeids)
      for (gradeTypeId <- toInputGradeTypeIds) {
        if (gradeTypeId == GradeTypeConstants.GA_ID) {
          //continue
        }
        val gradeType = entityDao.get(classOf[GradeType], gradeTypeId)
        params.append("&" + gradeType.getShortName + "Percent=" + get(gradeType.getShortName + "Percent"))
      }
    }
    redirect(new Action(getClass, if (submit) "submitResult" else "input", params.toString), "info.save.success")
  }

  def submitResult(): String = {
    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
    put("lesson", lesson)
    forward()
  }

  protected def getExistGradeMap(lesson: Lesson): Map[Student, CourseGrade] = {
    val existGrades = entityDao.get(classOf[CourseGrade], "lesson", lesson)
    val existGradeMap = CollectUtils.newHashMap()
    for (grade <- existGrades) {
      existGradeMap.put(grade.getStd, grade)
    }
    existGradeMap
  }

  protected def buildNewCourseGrade(take: CourseTake, status: Int, inputedAt: Date): CourseGrade = {
    val grade = new CourseGradeBean(take)
    val state = getGradeState
    grade.setMarkStyle(state.getScoreMarkStyle)
    grade.setStatus(status)
    grade.setProject(Model.newInstance(classOf[Project], take.getStd.getProject.id))
    val planCourseType = gradeCourseTypeProvider.getCourseType(take.getStd, take.getLesson.getCourse, 
      take.getLesson.getCourseType)
    grade.setCourseType(planCourseType)
    grade.setCreatedAt(inputedAt)
    grade
  }

  protected def updateGradeState(status: Int, inputedAt: Date) {
    val gradeState = getGradeState
    for (gradeType <- getGradeTypes(gradeState)) {
      if ((GradeTypeConstants.GA_ID) == gradeType.id) {
        gradeState.setStatus(status)
      }
      gradeState.getState(gradeType).setStatus(status)
      gradeState.getState(gradeType).setInputedAt(inputedAt)
      gradeState.getState(gradeType).setPrecision(gradeState.getPrecision)
    }
    gradeState.setInputedAt(inputedAt)
  }

  protected def buildCourseGrade(grade: CourseGrade, 
      take: CourseTake, 
      status: Int, 
      inputedAt: Date): CourseGrade = {
    val gradeState = getGradeState
    val gradeTypes = getGradeTypes(gradeState)
    val operator = getUsername
    if (null == grade) {
      grade = buildNewCourseGrade(take, status, inputedAt)
    } else {
      grade.setMarkStyle(gradeState.getScoreMarkStyle)
    }
    grade.setRemark(get("courseGrade.remark" + take.getStd.id))
    grade.setOperator(operator)
    grade.setUpdatedAt(inputedAt)
    for (gradeType <- gradeTypes) {
      buildExamGrade(grade, gradeType, take, status, inputedAt, operator)
    }
    if (CollectUtils.isEmpty(grade.getExamGrades)) {
      return null
    }
    if (grade.isTransient) grade.setCreatedAt(inputedAt)
    calculator.calc(grade, gradeState)
    grade
  }

  protected def buildExamGrade(grade: CourseGrade, 
      gradeType: GradeType, 
      take: CourseTake, 
      status: Int, 
      inputedAt: Date, 
      operator: String) {
    val scoreInputName = gradeType.getShortName + "_" + take.getStd.id
    val examScoreStr = get(scoreInputName)
    var examStatusId = getInt("examStatus_" + scoreInputName)
    if (null == examScoreStr && null == examStatusId && gradeType.id != GradeTypeConstants.GA_ID) {
      return
    }
    val examScore = getFloat(scoreInputName)
    var examStatus: ExamStatus = null
    if (null == examStatusId) {
      examStatusId = ExamStatus.NORMAL
    }
    examStatus = entityDao.get(classOf[ExamStatus], examStatusId)
    val markStyle = getState(gradeType).getScoreMarkStyle
    var examGrade = grade.getExamGrade(gradeType)
    if (null == examGrade) {
      examGrade = new ExamGradeBean(gradeType, examStatus, examScore)
      examGrade.setCreatedAt(inputedAt)
      grade.addExamGrade(examGrade)
    }
    grade.setUpdatedAt(inputedAt)
    val personPercent = getInt("personPercent_" + gradeType.getShortName + "_" + take.getStd.id)
    examGrade.setPercent(personPercent)
    examGrade.setMarkStyle(markStyle)
    examGrade.setExamStatus(examStatus)
    examGrade.setUpdatedAt(inputedAt)
    examGrade.setOperator(operator)
    examGrade.setScore(examScore)
    examGrade.setStatus(status)
  }

  protected def putGradeMap(lesson: Lesson, courseTakes: List[CourseTake]) {
    put("courseTakes", courseTakes)
    val grades = entityDao.get(classOf[CourseGrade], "lesson", lesson)
    val gradeMap = CollectUtils.newHashMap()
    for (grade <- grades) {
      gradeMap.put(grade.getStd, grade)
    }
    for (take <- courseTakes if !gradeMap.containsKey(take.getStd)) {
      gradeMap.put(take.getStd, new CourseGradeBean())
    }
    put("gradeMap", gradeMap)
  }

  def reportForExam(): String = {
    val lessonIdSeq = get("lesson.ids")
    if (Strings.isEmpty(lessonIdSeq)) {
      return forwardError("error.parameters.needed")
    }
    val lessons = entityDao.get(classOf[Lesson], Strings.splitToLong(lessonIdSeq))
    teachClassGradeHelper.statLesson(lessons, Array(GradeTypeConstants.END_ID))
    forward()
  }

  def removeGrade(): String = {
    val result = checkState()
    if (null != result) {
      return result
    }
    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
    val state = courseGradeService.getState(lesson)
    val gradeTypeIdSeq = get("gradeTypeIds")
    if (Strings.isNotBlank(gradeTypeIdSeq)) {
      val gradeTypeIds = Strings.splitToInt(get("gradeTypeIds"))
      for (i <- 0 until gradeTypeIds.length) {
        val gradeTypeState = state.getState(Model.newInstance(classOf[GradeType], gradeTypeIds(i)))
        if (null != gradeTypeState && Grade.Status.NEW != gradeTypeState.getStatus && 
          (gradeTypeState.getStatus > Grade.Status.CONFIRMED || 
          gradeTypeIds(i) != GradeTypeConstants.MAKEUP_ID && state.getStatus > Grade.Status.CONFIRMED)) {
          return forward(new Action(this, "inputTask"), "error.grade.modifyPublished")
        }
      }
    }
    val msg = courseGradeHelper.removeLessonGrade(getUserId)
    if (Strings.isEmpty(msg)) {
      logHelper.info("delete grade")
      redirect("inputTask", "info.delete.success", "&lessonId=" + lesson.id)
    } else {
      forwardError(msg)
    }
  }

  def report(): String = {
    var lessonIdSeq = get("lessonIds")
    if (Strings.isEmpty(lessonIdSeq)) lessonIdSeq = get("lessonId")
    if (Strings.isEmpty(lessonIdSeq)) {
      return forwardError("error.parameters.needed")
    }
    val lessons = entityDao.get(classOf[Lesson], Strings.splitToLong(lessonIdSeq))
    val gradeTypeIdSeq = getIntIds("gradeType")
    val gradeTypeIds = CollectUtils.newHashSet()
    if (null != gradeTypeIdSeq) gradeTypeIds.addAll(CollectUtils.newHashSet(gradeTypeIdSeq))
    if (gradeTypeIds.contains(GradeTypeConstants.MAKEUP_ID)) gradeTypeIds.add(GradeTypeConstants.DELAY_ID)
    teachClassGradeHelper.report(lessons, gradeTypeIds.toArray(Array.ofDim[Integer](gradeTypeIds.size)))
    val query = OqlBuilder.from(classOf[GradeRateConfig], "config")
      .where("config.project=:project", getProject)
    val gradeConfigMap = CollectUtils.newHashMap()
    for (config <- entityDao.search(query)) {
      gradeConfigMap.put(String.valueOf(config.getScoreMarkStyle.id), config)
    }
    put("gradeConfigMap", gradeConfigMap)
    put("GA_ID", GradeTypeConstants.GA_ID)
    if (gradeTypeIds.contains(GradeTypeConstants.MAKEUP_ID)) forward("../../../../lesson/web/action/report/reportMakeup") else forward("../../../../lesson/web/action/report/reportGa")
  }

  def info(): String = {
    val lesson = entityDao.get(classOf[Lesson], getLong("lessonId"))
    teachClassGradeHelper.info(lesson)
    forward()
  }

  def editReport(): String = forward()

  private def getGradeInputSwitch(project: Project, semester: Semester): GradeInputSwitch = {
    var gradeInputSwitch = gradeInputSwitchService.getSwitch(project, semester)
    if (null == gradeInputSwitch) {
      gradeInputSwitch = Model.newInstance(classOf[GradeInputSwitch])
      gradeInputSwitch.setProject(project)
      gradeInputSwitch.setSemester(semester)
      gradeInputSwitch.setTypes(CollectUtils.newHashSet(baseCodeService.getCodes(classOf[GradeType])))
    }
    gradeInputSwitch
  }

  private def getGradeInputSwitch(lesson: Lesson): GradeInputSwitch = {
    getGradeInputSwitch(lesson.getProject, lesson.getSemester)
  }

  private def getOrCreateState(lesson: Lesson): CourseGradeState = {
    var state = courseGradeService.getState(lesson)
    val precision = getInt("precision")
    val markStyleId = getInt("markStyleId")
    var markStyle: ScoreMarkStyle = null
    if (null != markStyleId) markStyle = entityDao.get(classOf[ScoreMarkStyle], markStyleId)
    if (null == state) state = new CourseGradeStateBean(lesson)
    if (null != markStyleStrategy) markStyleStrategy.configMarkStyle(state, getGradeTypes(state))
    val es = state.getState(new GradeType(GradeTypeConstants.GA_ID))
    if (null != markStyle) {
      state.setScoreMarkStyle(markStyle)
      if (null != es) es.setScoreMarkStyle(markStyle)
    }
    if (null != precision) {
      state.setPrecision(precision)
      if (null != es) es.setPrecision(precision)
    }
    entityDao.saveOrUpdate(state)
    state
  }

  def personPercent(): String = {
    val gradeTypes = getModels(classOf[GradeType], getLongIds("gradeType"))
    val courseGradeId = getLong("grade.id")
    if (null != courseGradeId) {
      val courseGrade = entityDao.get(classOf[CourseGrade], courseGradeId)
      put("courseGrade", courseGrade)
    }
    val gradeStateId = getLong("gradeStateId")
    if (null != gradeStateId) {
      put("gradeState", entityDao.get(classOf[CourseGradeState], gradeStateId))
    }
    put("student", entityDao.get(classOf[Student], getLong("student.id")))
    put("gradeTypes", gradeTypes)
    forward()
  }

  def reportContent(): String = {
    forward(new Action("", "reportForExam"))
  }
}
