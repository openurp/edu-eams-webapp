package org.openurp.eams.grade.teacher.action

import org.openurp.eams.grade.model.GradeRateConfig
import org.openurp.base.Semester
import org.openurp.teach.exam.ExamTake
import org.beangle.webmvc.api.action.ActionSupport
import org.openurp.teach.grade.model.ExamGradeBean
import org.openurp.eams.grade.service.CourseGradeSettings
import org.openurp.eams.grade.domain.CourseGradeCalculator
import org.openurp.eams.grade.ExamGradeState
import org.openurp.eams.grade.model.CourseGradeStateBean
import org.springframework.beans.support.PropertyComparator
import org.openurp.eams.grade.service.CourseGradeService
import org.openurp.teach.lesson.CourseTake
import org.openurp.teach.grade.Grade
import org.beangle.commons.lang.Strings
import org.openurp.teach.grade.model.CourseGradeBean
import org.openurp.teach.grade.domain.CourseGradeSubmitEvent
import org.openurp.teach.core.Project
import org.openurp.eams.grade.service.GradeRateService
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.eams.grade.CourseGradeState
import org.openurp.teach.lesson.Lesson
import org.openurp.teach.grade.domain.GradeCourseTypeProvider
import org.openurp.teach.grade.CourseGrade
import org.openurp.teach.code.ExamType
import org.openurp.teach.core.Student
import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.teach.code.CourseTakeType
import java.util.Collections
import org.openurp.teach.code.GradeType
import org.beangle.commons.lang.SystemInfo.User
import java.util.Date
import org.openurp.teach.code.ExamStatus
import org.openurp.eams.grade.domain.GradeTypePolicy
import java.awt.Desktop.Action
import org.openurp.eams.grade.domain.MarkStyleStrategy
import org.beangle.commons.bean.orderings.PropertyOrdering
import org.openurp.eams.grade.GradeInputSwitch
import org.openurp.eams.grade.model.GradeInputSwitchBean
import org.openurp.base.Teacher
import org.beangle.data.model.dao.EntityDao
import org.openurp.eams.grade.service.GradeInputSwitchService
import org.openurp.teach.code.service.BaseCodeService
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import org.openurp.eams.grade.model.ExamGradeStateBean
import org.openurp.teach.code.model.ScoreMarkStyleBean
import org.openurp.eams.grade.model.GaGradeStateBean
import org.openurp.eams.grade.GradeState
import org.openurp.teach.code.model.GradeTypeBean
import scala.collection.mutable.HashSet

/**
 * 教师管理成绩响应类
 *
 * @author chaostone
 */
class AbstractTeacherAction extends ActionSupport {

  var entityDao: EntityDao = _
  protected def checkLessonPermission(lesson: Lesson): String = {
    val teachers = entityDao.findBy(classOf[Teacher], "code", List("FIXME"))
    if (null == teachers.isEmpty) {
      return "只有教师才可以录入成绩"
    }
    if (!lesson.teachers.contains(teachers.head)) {
      return "没有权限"
    }
    null
  }

  //  var lessonFilterStrategyFactory: LessonFilterStrategyFactory = _

  //  var makeupStdStrategy: MakeupStdStrategy = _

  var gradeInputSwitchService: GradeInputSwitchService = _

  //  var lessonService: LessonService = _

  var courseGradeService: CourseGradeService = _

  var baseCodeService: BaseCodeService = _

  //  var lessonGradeService: LessonGradeService = _

  //  var courseGradeHelper: CourseGradeHelper = _

  var calculator: CourseGradeCalculator = _

  var gradeRateService: GradeRateService = _

  //var teachClassGradeHelper: TeachClassGradeHelper = _

  var gradeTypePolicy: GradeTypePolicy = _

  var markStyleStrategy: MarkStyleStrategy = _

  var settings: CourseGradeSettings = _

  var gradeCourseTypeProvider: GradeCourseTypeProvider = _

  protected def checkState(): String = {
    val lessonId = getInt("lessonId").get
    val lesson = entityDao.get(classOf[Lesson], new Integer(lessonId))
    if (null == lesson) {
      throw new IllegalArgumentException("lession is null")
    }
    val msg = checkLessonPermission(lesson)
    if (null != msg) {
      throw new IllegalArgumentException(msg)
    }
    val gis = getGradeInputSwitch(lesson)
    put("gradeInputSwitch", gis)
    if (!gis.opened) {
      return forward("../teacher/cannotInput")
    }
    val gradeState = getOrCreateState(lesson)
    put("gradeState", gradeState)
    for (
      gradeType <- getGradeTypes(gradeState) if null != getState(gradeType) && Grade.Status.New != getState(gradeType).status &&
        getState(gradeType).status > Grade.Status.Confirmed
    ) {
      //      return redirect(new Action(classOf[AdminAction], "inputTask", "&lessonId=" + lesson.getId), "error.grade.modifyPublished")
      throw new IllegalArgumentException("error.grade.modifyPublished")
    }
    null
  }

  /**
   * 查找和创建指定类型的成绩状态
   *
   * @param gradeType
   * @return
   */
  protected def getState(gradeType: GradeType): GradeState = {
    val gradeState = getGradeState
    val gradeTypeState = Option(gradeState.getState(gradeType)).getOrElse({
      val state = if (gradeType.isGa) {
        val gradeTypeState = new GaGradeStateBean
        gradeState.gaStates += gradeTypeState
        gradeTypeState.gradeType = gradeType
        gradeTypeState.gradeState = getGradeState
        gradeTypeState.scoreMarkStyle = new ScoreMarkStyleBean(gradeState.scoreMarkStyle.id)
        gradeTypeState
      } else {
        val gradeTypeState = new ExamGradeStateBean
        gradeState.examStates += gradeTypeState
        gradeTypeState.gradeType = gradeType
        gradeTypeState.gradeState = getGradeState
        gradeTypeState
      }
      state.status = Grade.Status.New
      state.updatedAt = new Date()
      state
    })
    gradeTypeState
  }

  //  protected def getStates(): List[ExamGradeState] = {
  //    val gradeState = getGradeState
  //    val myStats = CollectUtils.newArrayList()
  //    for (gradeType <- getGradeTypes(gradeState)) {
  //      var examGradeState = gradeState.getState(gradeType)
  //      if (null == examGradeState) {
  //        examGradeState = Model.newInstance(classOf[ExamGradeState])
  //        examGradeState.setGradeType(gradeType)
  //        examGradeState.setGradeState(gradeState)
  //        entityDao.saveOrUpdate(gradeState)
  //      }
  //      myStats.add(examGradeState)
  //    }
  //    myStats
  //  }

  /**
   * 得到允许录入的成绩类型
   */
  protected def getGradeTypes(gradeState: CourseGradeState): Seq[GradeType] = {
    val gradeTypes = getAttribute("gradeTypes").asInstanceOf[Seq[GradeType]]
    if (null == gradeTypes) {
      val gradeTypes = new ListBuffer[GradeType]
      val gradeTypeIds = Strings.splitToInt(get("gradeTypeIds").get)
      val gradeInputSwitch = getAttribute("gradeInputSwitch").asInstanceOf[GradeInputSwitch]
      for (typeId <- gradeTypeIds) {
        val gradeType = baseCodeService.getCode(classOf[GradeType], typeId).asInstanceOf[GradeType]
        if (gradeInputSwitch.types.contains(gradeType)) gradeTypes += gradeType
      }
      gradeTypes
    } else gradeTypes
  }

  //  protected def removeZeroPercentTypes(gradeState: CourseGradeState, gradeTypes: List[GradeType]) {
  //    if (CollectUtils.isEmpty(gradeTypes)) {
  //      return
  //    }
  //    val zeroPercentTypes = CollectUtils.newArrayList()
  //    for (`type` <- gradeTypes) {
  //      val egState = gradeState.getState(`type`)
  //      if (null != egState.getPercent) {
  //        if (egState.getPercent == 0F) {
  //          zeroPercentTypes.add(egState.getGradeType)
  //        }
  //      }
  //    }
  //    for (`type` <- zeroPercentTypes) {
  //      gradeTypes.remove(`type`)
  //      val egState = gradeState.getState(`type`)
  //      if (null != egState) gradeState.getStates.remove(egState)
  //    }
  //  }

  protected def getCourseTakes(lesson: Lesson): Seq[CourseTake] = {
    val takes = lesson.teachClass.courseTakes.toList
    takes.sorted(new PropertyOrdering("std.code"))
    takes
  }

  /**
   * 加载录入成绩首页面
   */
  //  def innerIndex(): String = {
  //    val teacher = getLoginTeacher
  //    if (teacher == null) {
  //      return forwardError("没有权限")
  //    }
  //    putSemester(getProject)
  //    forward()
  //  }

  //  def taskList(): String = {
  //    val teacher = getLoginTeacher
  //    val user = entityDao.get(classOf[User], getUserId)
  //    val semesterId = getInt("lesson.semester.id")
  //    if (semesterId == null) {
  //      return forwardError("error.teacher.noTask")
  //    }
  //    val semester = entityDao.get(classOf[Semester], semesterId)
  //    val lessons = lessonService.getLessonByCategory(teacher.getId, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.TEACHER),
  //      semester)
  //    val builder = OqlBuilder.from(classOf[CourseGradeState], "courseGradeState")
  //    builder.where("courseGradeState.extraInputer = :user", user)
  //    if (!lessons.isEmpty) {
  //      builder.where("courseGradeState.lesson not in (:lessons)", lessons)
  //    }
  //    builder.where("courseGradeState.lesson.semester=:semester", semester)
  //    builder.select("courseGradeState.lesson")
  //    lessons.addAll(entityDao.search(builder).asInstanceOf[List[Lesson]])
  //    CollectionUtils.filter(lessons, new LessonProjectPredicate(getProject))
  //    put("lessons", lessons)
  //    val gradeStates = CollectUtils.newHashMap()
  //    for (lesson <- lessons) {
  //      val state = courseGradeService.getState(lesson)
  //      if (state != null) gradeStates.put(lesson.getId, state)
  //    }
  //    put("makeupTakeCounts", makeupStdStrategy.getCourseTakeCounts(lessons))
  //    put("gradeStates", gradeStates)
  //    put("gradeInputSwitch", getGradeInputSwitch(getProject, semester))
  //    put("GA", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.GA_ID))
  //    put("MAKEUP", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.MAKEUP_ID))
  //    put("DELAY", baseCodeService.getCode(classOf[GradeType], GradeTypeConstants.DELAY_ID))
  //    forward()
  //  }

  /**
   * 组装一些前台需要的参数
   */
  protected def buildSomeParams(lesson: Lesson, putScomeParams: Set[String]) {
    val state = courseGradeService.getState(lesson)
    if (null == putScomeParams) {
      return
    }
    if (putScomeParams.contains("gradeConverterConfig")) {
      val query = OqlBuilder.from(classOf[GradeRateConfig], "config")
      query.where("config.scoreMarkStyle = :markStyle", state.scoreMarkStyle)
      val configs = entityDao.search(query)
      if (configs != null && !configs.isEmpty) {
        put("gradeConverterConfig", configs(0))
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
      put("PERCENT", baseCodeService.getCode(classOf[ScoreMarkStyle], ScoreMarkStyle.Percent ))
    }
    //FIX ME
//    if (putScomeParams.contains("RANK_EN")) {
//      put("RANK_EN", baseCodeService.getCode(classOf[ScoreMarkStyle], ScoreMarkStyle.RANK_EN))
//    }
//    if (putScomeParams.contains("GA")) {
//      put("GA", baseCodeService.getCode(classOf[GradeType], GradeType.EndGa ))
//    }
//    if (putScomeParams.contains("MIDDLE")) {
//      put("MIDDLE", baseCodeService.getCode(classOf[GradeType], GradeType.MIDDLE_ID))
//    }
//    if (putScomeParams.contains("USUAL")) {
//      put("USUAL", baseCodeService.getCode(classOf[GradeType], GradeType.USUAL_ID))
//    }
//    if (putScomeParams.contains("END")) {
//      put("END", baseCodeService.getCode(classOf[GradeType], GradeType.END_ID))
//    }
//    if (putScomeParams.contains("MAKEUP")) {
//      put("MAKEUP", baseCodeService.getCode(classOf[GradeType], GradeType.MAKEUP_ID))
//    }
//    if (putScomeParams.contains("RESTUDY")) {
//      put("RESTUDY", CourseTakeType.RESTUDY)
//    }
//    if (putScomeParams.contains("REEXAM")) {
//      put("REEXAM", CourseTakeType.REEXAM)
//    }
//    if (putScomeParams.contains("NORMAL")) {
//      put("NORMAL", ExamStatus.NORMAL)
//    }
//    if (putScomeParams.contains("ABSENT")) {
//      put("ABSENT", ExamStatus.ABSENT)
//    }
//    if (putScomeParams.contains("CHEAT")) {
//      put("CHEAT", ExamStatus.CHEAT)
//    }
//    if (putScomeParams.contains("DELAY")) {
//      put("DELAY", ExamStatus.DELAY)
//    }
//    if (putScomeParams.contains("VIOLATION")) {
//      put("VIOLATION", ExamStatus.VIOLATION)
//    }
//    if (putScomeParams.contains("NEW")) {
//      put("NEW", Grade.Status.NEW)
//    }
//    if (putScomeParams.contains("CONFIRMED")) {
//      put("CONFIRMED", Grade.Status.CONFIRMED)
//    }
//    if (putScomeParams.contains("examStatuses")) {
//      val query = OqlBuilder.from(classOf[ExamStatus], "examStatus")
//      query.where("examStatus.effectiveAt <= :now and (examStatus.invalidAt is null or examStatus.invalidAt >= :now)",
//        new Date())
//      query.orderBy("examStatus.code")
//      put("examStatuses", entityDao.search(query))
//    }
//    if (putScomeParams.contains("isTeacher")) {
//      put("isTeacher", false)
//    }
//    if (putScomeParams.contains("gradeRateConfigs")) {
//      val gradeRateConfigs = CollectUtils.newHashMap()
//      val gradeTypes = getGradeTypes(state)
//      for (gradeType <- gradeTypes) {
//        if (GradeTypeConstants.GA_ID == gradeType.getId) {
//          //continue
//        }
//        val examGradeState = state.getState(gradeType)
//        val scoreMarkStyle = entityDao.get(classOf[ScoreMarkStyle], examGradeState.getScoreMarkStyle.getId)
//        if (!scoreMarkStyle.isNumStyle) {
//          if (!gradeRateConfigs.containsKey(scoreMarkStyle)) {
//            gradeRateConfigs.put(scoreMarkStyle, gradeRateService.getConfig(getProject, scoreMarkStyle))
//          }
//        }
//        put("gradeRateConfigs", gradeRateConfigs)
//      }
//    }
  }

  /**
   * 处理除百分比记录方式外的录入项
   *
   * @param task
   * @param gradeTypes
   */
  protected def buildGradeConfig(lesson: Lesson, gradeTypes: List[GradeType]) {
    val gradeState = getGradeState
    val markStyles = new HashMap()
    val examTypes = new HashSet()
    //FIX ME 
//    for (gradeType <- gradeTypes) {
//      val gradeTypeState = gradeState.getState(gradeType)
//      if (null == gradeTypeState) {
//        //continue
//      }
//      var markStyle = gradeTypeState.scoreMarkStyle
//      if (null == markStyle) markStyle = gradeState.scoreMarkStyle
//      if (null != gradeType.examType) examTypes.add(gradeType.examType)
//    }
//    val converterMap = new HashMap()
//    for (gradeTypeState <- gradeState.getStates) {
//      markStyles.put(gradeTypeState.getGradeType.getId.toString, gradeTypeState.getScoreMarkStyle)
//    }
//    put("markStyles", markStyles)
//    put("converterMap", converterMap)
//    put("stdExamTypeMap", getStdExamTypeMap(lesson, examTypes))
  }

  //  /**
  //   * 根据教学任务、教学任务教学班学生和考试类型组装一个Map
  //   *
  //   * @param task
  //   * @param examTypes
  //   * @return
  //   */
  //  protected def getStdExamTypeMap(lesson: Lesson, examTypes: Set[ExamType]): Map[String, ExamTake] = {
  //    if (CollectUtils.isEmpty(lesson.getTeachClass.getCourseTakes) ||
  //      examTypes.isEmpty) {
  //      return CollectUtils.newHashMap()
  //    }
  //    val query = OqlBuilder.from(classOf[ExamTake], "examTake").where("examTake.lesson=:lesson", lesson)
  //    if (CollectUtils.isNotEmpty(examTypes)) {
  //      query.where("examTake.examType in (:examTypes)", examTypes)
  //    }
  //    val stdExamTypeMap = CollectUtils.newHashMap()
  //    val examTakes = entityDao.search(query)
  //    for (examTake <- examTakes) {
  //      stdExamTypeMap.put(examTake.getStd.getId + "_" + examTake.getExamType.getId, examTake)
  //    }
  //    stdExamTypeMap
  //  }

  //  /**
  //   * 录入单个教学任务成绩
  //   *
  //   * @return @
  //   */
  //  def inputTask(): String = {
  //    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
  //    val msg = checkLessonPermission(lesson)
  //    if (null != msg) {
  //      return forwardError(msg)
  //    }
  //    val gradeInputSwitch = getGradeInputSwitch(lesson)
  //    put("gradeInputSwitch", gradeInputSwitch)
  //    put("gradeState", getOrCreat eState (lesson))
  //    val putSomeParams = CollectUtils.newHashSet()
  //    putSomeParams.add("MAKEUP")
  //    putSomeParams.add("GA")
  //    putSomeParams.add("isTeacher")
  //    put("markStyles", gradeRateService.getMarkStyles(lesson.getProject))
  //    buildSomeParams(lesson, putSomeParams)
  //    put("DELAY_ID", GradeTypeConstants.DELAY_ID)
  //    val gaGradeTypes = settings.getSetting(getProject).getGaElementTypes
  //    val gaGradeTypeParams = CollectUtils.newArrayList()
  //    for (gradeType <- gaGradeTypes) {
  //      gradeType = entityDao.get(classOf[GradeType], gradeType.getId)
  //      if (gradeInputSwitch.getTypes.contains(gradeType)) gaGradeTypeParams.add(gradeType)
  //    }
  //    put("gaGradeTypes", gaGradeTypeParams)
  //    put("lesson", lesson)
  //    forward()
  //  }

  //  /**
  //   * 保存成绩
  //   */
  //  def save(): String = {
  //    val result = checkState()
  //    if (null != result) {
  //      return result
  //    }
  //    val submit = !getBool("justSave")
  //    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
  //    val msg = checkLessonPermission(lesson)
  //    if (null != msg) {
  //      return forwardError(msg)
  //    }
  //    val existGradeMap = getExistGradeMap(lesson)
  //    val setting = settings.getSetting(getProject)
  //    val isPublish = setting.isSubmitIsPublish
  //    if (submit) {
  //      val gradeState = courseGradeService.getState(lesson)
  //      val examGradeStates = gradeState.getStates
  //      val existGradeTypes = CollectUtils.collect(examGradeStates, new PropertyTransformer("gradeType"))
  //      if (!existGradeTypes.contains(Model.newInstance(classOf[GradeType], GradeTypeConstants.MAKEUP_ID)) &&
  //        !existGradeTypes.contains(Model.newInstance(classOf[GradeType], GradeTypeConstants.DELAY_ID))) {
  //        for (courseGrade <- existGradeMap.values; gradeType <- setting.getGaElementTypes) {
  //          val examGradeState = gradeState.getState(gradeType)
  //          val examGrade = courseGrade.getExamGrade(gradeType)
  //          if (examGradeState != null && null != examGrade) {
  //            if (examGradeState.getPercent == null || examGradeState.getPercent == 0) {
  //              courseGrade.getExamGrades.remove(examGrade)
  //            } else {
  //              examGrade.setPercent(null)
  //            }
  //          }
  //        }
  //      }
  //    }
  //    val inputedAt = new Date()
  //    val grades = CollectUtils.newArrayList()
  //    val status = if (submit) Grade.Status.CONFIRMED else Grade.Status.NEW
  //    val takes = getCourseTakes(lesson)
  //    for (take <- takes) {
  //      val grade = buildCourseGrade(existGradeMap.get(take.getStd), take, status, inputedAt)
  //      if (null != lesson.getExamMode) {
  //        grade.setExamMode(lesson.getExamMode)
  //      }
  //      if (null != grade) grades.add(grade)
  //    }
  //    if (submit) {
  //      updateGradeState(Grade.Status.CONFIRMED, inputedAt)
  //    } else {
  //      updateGradeState(Grade.Status.NEW, inputedAt)
  //    }
  //    val gradeState = getGradeState
  //    val operator = getUsername
  //    gradeState.setOperator(operator)
  //    val params = new StringBuilder("&lessonId=" + lesson.getId)
  //    params.append("&gradeTypeIds=")
  //    val inputableGradeTypes = getGradeTypes(gradeState)
  //    for (gradeType <- inputableGradeTypes) {
  //      getState(gradeType).setOperator(operator)
  //      params.append(gradeType.getId + ",")
  //    }
  //    entityDao.saveOrUpdate(grades, gradeState)
  //    if (submit) {
  //      if (isPublish) {
  //        val publishableGradeTypes = CollectionUtils.intersection(inputableGradeTypes, setting.getPublishableTypes).asInstanceOf[List[_]]
  //        var alreadyContainGA = false
  //        var alreadyContainFINAL = false
  //        for (publishGradeType <- publishableGradeTypes) {
  //          if (publishGradeType.getId == GradeTypeConstants.FINAL_ID) {
  //            alreadyContainFINAL = true
  //          } else if (publishGradeType.getId == GradeTypeConstants.GA_ID) {
  //            alreadyContainGA = true
  //          }
  //        }
  //        if (!alreadyContainGA) {
  //          publishableGradeTypes.add(Model.newInstance(classOf[GradeType], GradeTypeConstants.GA_ID))
  //        }
  //        if (!alreadyContainFINAL) {
  //          publishableGradeTypes.add(Model.newInstance(classOf[GradeType], GradeTypeConstants.FINAL_ID))
  //        }
  //        courseGradeService.publish(lesson.getId + "", publishableGradeTypes.toArray(Array()), true)
  //      }
  //      publish(new CourseGradeSubmitEvent(gradeState))
  //    }
  //    params.deleteCharAt(params.length - 1)
  //    val toInputGradeTypeids = get("toInputGradeType.id")
  //    if (Strings.isNotEmpty(toInputGradeTypeids)) {
  //      params.append("&toInputGradeType.ids=" + get("toInputGradeType.id"))
  //      val toInputGradeTypeIds = Strings.splitToInt(toInputGradeTypeids)
  //      for (gradeTypeId <- toInputGradeTypeIds) {
  //        if (gradeTypeId == GradeTypeConstants.GA_ID) {
  //          //continue
  //        }
  //        val gradeType = entityDao.get(classOf[GradeType], gradeTypeId)
  //        params.append("&" + gradeType.getShortName + "Percent=" + get(gradeType.getShortName + "Percent"))
  //      }
  //    }
  //    redirect(new Action(getClass, if (submit) "submitResult" else "input", params.toString), "info.save.success")
  //  }

  //  /**
  //   * 提交以后的结果
  //   *
  //   * @return
  //   */
  //  def submitResult(): String = {
  //    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
  //    val msg = checkLessonPermission(lesson)
  //    if (null != msg) {
  //      return forwardError(msg)
  //    }
  //    put("lesson", lesson)
  //    forward()
  //  }
  //
  //  protected def getExistGradeMap(lesson: Lesson): Map[Student, CourseGrade] = {
  //    val existGrades = entityDao.get(classOf[CourseGrade], "lesson", lesson)
  //    val existGradeMap = CollectUtils.newHashMap()
  //    for (grade <- existGrades) {
  //      existGradeMap.put(grade.getStd, grade)
  //    }
  //    existGradeMap
  //  }
  //
  //  /**
  //   * 新增成绩
  //   */
  //  protected def buildNewCourseGrade(take: CourseTake, status: Int, inputedAt: Date): CourseGrade = {
  //    val grade = new CourseGradeBean(take)
  //    val state = getGradeState
  //    grade.setMarkStyle(state.getScoreMarkStyle)
  //    grade.setStatus(status)
  //    grade.setProject(Model.newInstance(classOf[Project], take.getStd.getProject.getId))
  //    val planCourseType = gradeCourseTypeProvider.getCourseType(take.getStd, take.getLesson.getCourse,
  //      take.getLesson.getCourseType)
  //    grade.setCourseType(planCourseType)
  //    grade.setCreatedAt(inputedAt)
  //    grade
  //  }
  //
  //  protected def updateGradeState(status: Int, inputedAt: Date) {
  //    val gradeState = getGradeState
  //    for (gradeType <- getGradeTypes(gradeState)) {
  //      if ((GradeTypeConstants.GA_ID) == gradeType.getId) {
  //        gradeState.setStatus(status)
  //      }
  //      gradeState.getState(gradeType).setStatus(status)
  //      gradeState.getState(gradeType).setInputedAt(inputedAt)
  //      gradeState.getState(gradeType).setPrecision(gradeState.getPrecision)
  //    }
  //    gradeState.setInputedAt(inputedAt)
  //  }
  //
  //  /**
  //   * 每一个学生的成绩
  //   */
  //  protected def buildCourseGrade(grade: CourseGrade,
  //                                 take: CourseTake,
  //                                 status: Int,
  //                                 inputedAt: Date): CourseGrade = {
  //    val gradeState = getGradeState
  //    val gradeTypes = getGradeTypes(gradeState)
  //    val operator = getUsername
  //    if (null == grade) {
  //      grade = buildNewCourseGrade(take, status, inputedAt)
  //    } else {
  //      grade.setMarkStyle(gradeState.getScoreMarkStyle)
  //    }
  //    grade.setRemark(get("courseGrade.remark" + take.getStd.getId))
  //    grade.setOperator(operator)
  //    grade.setUpdatedAt(inputedAt)
  //    for (gradeType <- gradeTypes) {
  //      buildExamGrade(grade, gradeType, take, status, inputedAt, operator)
  //    }
  //    if (CollectUtils.isEmpty(grade.getExamGrades)) {
  //      return null
  //    }
  //    if (grade.isTransient) grade.setCreatedAt(inputedAt)
  //    calculator.calc(grade, gradeState)
  //    grade
  //  }
  //
  //  /**
  //   * 每一个成绩类型
  //   */
  //  protected def buildExamGrade(grade: CourseGrade,
  //                               gradeType: GradeType,
  //                               take: CourseTake,
  //                               status: Int,
  //                               inputedAt: Date,
  //                               operator: String) {
  //    val scoreInputName = gradeType.getShortName + "_" + take.getStd.getId
  //    val examScoreStr = get(scoreInputName)
  //    var examStatusId = getInt("examStatus_" + scoreInputName)
  //    if (null == examScoreStr && null == examStatusId && gradeType.getId != GradeTypeConstants.GA_ID) {
  //      return
  //    }
  //    val examScore = getFloat(scoreInputName)
  //    var examStatus: ExamStatus = null
  //    if (null == examStatusId) {
  //      examStatusId = ExamStatus.NORMAL
  //    }
  //    examStatus = entityDao.get(classOf[ExamStatus], examStatusId)
  //    val markStyle = getState(gradeType).getScoreMarkStyle
  //    var examGrade = grade.getExamGrade(gradeType)
  //    if (null == examGrade) {
  //      examGrade = new ExamGradeBean(gradeType, examStatus, examScore)
  //      examGrade.setCreatedAt(inputedAt)
  //      grade.addExamGrade(examGrade)
  //    }
  //    grade.setUpdatedAt(inputedAt)
  //    val personPercent = getInt("personPercent_" + gradeType.getShortName + "_" + take.getStd.getId)
  //    examGrade.setPercent(personPercent)
  //    examGrade.setMarkStyle(markStyle)
  //    examGrade.setExamStatus(examStatus)
  //    examGrade.setUpdatedAt(inputedAt)
  //    examGrade.setOperator(operator)
  //    examGrade.setScore(examScore)
  //    examGrade.setStatus(status)
  //  }
  //
  protected def putGradeMap(lesson: Lesson, courseTakes: List[CourseTake]) {
    put("courseTakes", courseTakes)
    val grades = entityDao.findBy(classOf[CourseGrade], "lesson.id", List(lesson.id))
    val gradeMap = new HashMap[Student, CourseGrade]()
    for (grade <- grades) {
      gradeMap.put(grade.std, grade)
    }
    for (take <- courseTakes if !gradeMap.contains(take.std)) {
      gradeMap.put(take.std, new CourseGradeBean())
    }
    put("gradeMap", gradeMap)
  }

  //  /**
  //   * 试卷分析打印<br>
  //   * 分析期末成绩
  //   */
  //  def reportForExam(): String = {
  //    val lessonIdSeq = get("lesson.ids")
  //    if (Strings.isEmpty(lessonIdSeq)) {
  //      return forwardError("error.parameters.needed")
  //    }
  //    val lessons = entityDao.get(classOf[Lesson], Strings.splitToLong(lessonIdSeq))
  //    teachClassGradeHelper.statLesson(lessons, Array(GradeTypeConstants.END_ID))
  //    forward()
  //  }

  //  /**
  //   * 删除成绩
  //   */
  //  def removeGrade(): String = {
  //    val result = checkState()
  //    if (null != result) {
  //      return result
  //    }
  //    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
  //    var msg = checkLessonPermission(lesson)
  //    if (null != msg) {
  //      return forwardError(msg)
  //    }
  //    val state = courseGradeService.getState(lesson)
  //    val gradeTypeIdSeq = get("gradeTypeIds")
  //    if (Strings.isNotBlank(gradeTypeIdSeq)) {
  //      val gradeTypeIds = Strings.splitToInt(get("gradeTypeIds"))
  //      for (i <- 0 until gradeTypeIds.length) {
  //        val gradeTypeState = state.getState(Model.newInstance(classOf[GradeType], gradeTypeIds(i)))
  //        if (null != gradeTypeState && Grade.Status.NEW != gradeTypeState.getStatus &&
  //          (gradeTypeState.getStatus > Grade.Status.CONFIRMED ||
  //            gradeTypeIds(i) != GradeTypeConstants.MAKEUP_ID && state.getStatus > Grade.Status.CONFIRMED)) {
  //          return forward(new Action(this, "inputTask"), "error.grade.modifyPublished")
  //        }
  //      }
  //    }
  //    msg = courseGradeHelper.removeLessonGrade(getUserId)
  //    if (Strings.isEmpty(msg)) {
  //      logHelper.info("delete grade")
  //      redirect("inputTask", "info.delete.success", "&lessonId=" + lesson.getId)
  //    } else {
  //      forwardError(msg)
  //    }
  //  }

  //  /**
  //   * 打印教学班成绩
  //   */
  //  def report(): String = {
  //    var lessonIdSeq = get("lessonIds")
  //    if (Strings.isEmpty(lessonIdSeq)) lessonIdSeq = get("lessonId")
  //    if (Strings.isEmpty(lessonIdSeq)) {
  //      return forwardError("error.parameters.needed")
  //    }
  //    val lessons = entityDao.get(classOf[Lesson], Strings.splitToLong(lessonIdSeq))
  //    val gradeTypeIdSeq = getIntIds("gradeType")
  //    val gradeTypeIds = CollectUtils.newHashSet()
  //    if (null != gradeTypeIdSeq) gradeTypeIds.addAll(CollectUtils.newHashSet(gradeTypeIdSeq))
  //    if (gradeTypeIds.contains(GradeTypeConstants.MAKEUP_ID)) gradeTypeIds.add(GradeTypeConstants.DELAY_ID)
  //    teachClassGradeHelper.report(lessons, gradeTypeIds.toArray(Array.ofDim[Integer](gradeTypeIds.size)))
  //    val query = OqlBuilder.from(classOf[GradeRateConfig], "config")
  //      .where("config.project=:project", getProject)
  //    val gradeConfigMap = CollectUtils.newHashMap()
  //    for (config <- entityDao.search(query)) {
  //      gradeConfigMap.put(String.valueOf(config.getScoreMarkStyle.getId), config)
  //    }
  //    put("gradeConfigMap", gradeConfigMap)
  //    put("GA_ID", GradeTypeConstants.GA_ID)
  //    if (gradeTypeIds.contains(GradeTypeConstants.MAKEUP_ID)) forward("../../../../lesson/web/action/report/reportMakeup") else forward("../../../../lesson/web/action/report/reportGa")
  //  }

  //  /**
  //   * 查看单个教学任务所有成绩信息
  //   */
  //  def info(): String = {
  //    val lesson = entityDao.get(classOf[Lesson], getLong("lessonId"))
  //    val msg = checkLessonPermission(lesson)
  //    if (null != msg) {
  //      return forwardError(msg)
  //    }
  //    teachClassGradeHelper.info(lesson)
  //    forward()
  //  }

  //  /**
  //   * 跳转到编辑打印内容页面
  //   */
  //  def editReport(): String = forward()

  private def getGradeInputSwitch(project: Project, semester: Semester): GradeInputSwitch = {
    var gradeInputSwitch = gradeInputSwitchService.getSwitch(project, semester)
    if (null == gradeInputSwitch) {
      gradeInputSwitch = new GradeInputSwitchBean
      gradeInputSwitch.project = project
      gradeInputSwitch.semester = semester
      gradeInputSwitch.types = new collection.mutable.HashSet[GradeType]
      gradeInputSwitch.types ++= baseCodeService.getCodes(project, classOf[GradeType])
    }
    gradeInputSwitch
  }

  private def getGradeInputSwitch(lesson: Lesson): GradeInputSwitch = {
    getGradeInputSwitch(lesson.project, lesson.semester)
  }

  /**
   * 查找和创建成绩状态
   */
  private def getOrCreateState(lesson: Lesson): CourseGradeState = {
    var state = courseGradeService.getState(lesson)
    val precision = getInt("precision")
    val markStyleId = getInt("markStyleId").get
    var markStyle: ScoreMarkStyle = null
    if (null != markStyleId) markStyle = entityDao.get(classOf[ScoreMarkStyle], new Integer(markStyleId))
    if (null == state) state = new CourseGradeStateBean(lesson)
    if (null != markStyleStrategy) markStyleStrategy.configMarkStyle(state, getGradeTypes(state))
    val es = state.getState(new GradeTypeBean(GradeType.EndGa))
    if (null != markStyle) {
      state.scoreMarkStyle = markStyle
      if (null != es) es.scoreMarkStyle = markStyle
    }
    entityDao.saveOrUpdate(state)
    state
  }

  //  /**
  //   * 提交编辑的打印内容，到打印页面
  //   *
  //   * @param mapping
  //   * @param form
  //   * @param request
  //   * @param response
  //   * @return @
  //   */
  //  def reportContent(): String = {
  //    forward(new Action("", "reportForExam"))
  //  }

  protected def getGradeState(): CourseGradeStateBean = {
    getAttribute("gradeState").asInstanceOf[CourseGradeStateBean]
  }

  protected def getProject() = {
    entityDao.get(classOf[Project], new Integer(1))
  }
  
  def getUserCategoryId = 1

}
