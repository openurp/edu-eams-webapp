package org.openurp.edu.eams.teach.grade.lesson.web.action


import java.util.Date


import org.beangle.commons.bean.transformers.PropertyTransformer
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.struts2.convention.route.Action
import org.beangle.struts2.helper.QueryHelper
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.system.security.EamsUserCategory
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.course.service.MakeupStdStrategy
import org.openurp.edu.eams.teach.grade.course.web.helper.CourseGradeHelper
import org.openurp.edu.eams.teach.grade.course.web.helper.TeachClassGradeHelper
import org.openurp.edu.eams.teach.grade.lesson.service.LessonGradeService
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import org.openurp.edu.eams.teach.grade.service.GradeRateService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operator
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.grade.model.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseGradeStateBean
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class InputAction extends SemesterSupportAction {

  var courseGradeService: CourseGradeService = _

  var lessonGradeService: LessonGradeService = _

  var calculator: CourseGradeCalculator = _

  var lessonService: LessonService = _

  var teachClassGradeHelper: TeachClassGradeHelper = _

  var courseGradeHelper: CourseGradeHelper = _

  var makeupStdStrategy: MakeupStdStrategy = _

  var gradeRateService: GradeRateService = _

  var settings: CourseGradeSettings = _

  def batchEditExtraInputer(): String = {
    val courseGradeStates = Collections.newBuffer[Any]
    val lessons = getModels(classOf[Lesson], getLongIds("lesson"))
    for (lesson <- lessons) {
      var courseGradeState = courseGradeService.getState(lesson)
      if (null == courseGradeState) {
        courseGradeState = Model.newInstance(classOf[CourseGradeStateBean])
        courseGradeState.setLesson(lesson)
        courseGradeState.setScoreMarkStyle(Model.newInstance(classOf[ScoreMarkStyle], ScoreMarkStyle.PERCENT))
      }
      courseGradeStates.add(courseGradeState)
    }
    entityDao.saveOrUpdate(courseGradeStates)
    put("courseGradeStates", courseGradeStates)
    forward()
  }

  def batchSaveExtraInputer(): String = {
    val courseGradeStates = getModels(classOf[CourseGradeState], getLongIds("courseGradeState"))
    for (courseGradeState <- courseGradeStates) {
      val userId = getLong("courseGradeState.extraInputer.id" + courseGradeState.id)
      if (userId == null) {
        courseGradeState.setExtraInputer(null)
      } else {
        courseGradeState.setExtraInputer(entityDao.get(classOf[User], userId))
      }
    }
    try {
      entityDao.saveOrUpdate(courseGradeStates)
    } catch {
      case e: Exception => return redirect("search", "info.save.failure")
    }
    redirect("search", "info.save.success")
  }

  private def buildNoGradeCourseTakeQuery(): OqlBuilder[CourseTake] = {
    val departments = getDeparts
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    populateConditions(query)
    if (null == getProject || Collections.isEmpty(departments)) {
      query.where("take is null")
    } else {
      val semeterId = getInt("lesson.semester.id")
      if (null != semeterId) {
        put("semeterId", semeterId)
        query.where("take.lesson.semester.id = :semesterId", semeterId)
      }
      query.where("not exists(from " + classOf[CourseGrade].getName + 
        " cg where cg.std = take.std and cg.lesson = take.lesson)")
      val conditions = QueryHelper.extractConditions(classOf[Lesson], "lesson", null)
      if (Collections.isNotEmpty(conditions)) {
        query.join("take.lesson", "lesson")
        query.where(conditions)
      }
      query.where("take.std.project =:project", getProject)
      query.where("take.std.department in (:departments)", departments)
    }
    query.orderBy(Order.parse(get("orderBy"))).limit(getPageLimit)
    query
  }

  private def buildUnPassedGradeQuery(): OqlBuilder[CourseGrade] = {
    val project = getProject
    val departments = getDeparts
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    populateConditions(query)
    if (null == project || Collections.isEmpty(departments)) {
      query.where("grade is null")
    } else {
      val semesterId = getInt("lesson.semester.id")
      if (null != semesterId) {
        put("semesterId", semesterId)
        query.where("grade.semester.id = :semesterId", semesterId)
      }
      query.where("grade.score is not null")
      query.where("grade.passed = false")
      val conditions = QueryHelper.extractConditions(classOf[Lesson], "lesson", null)
      if (Collections.isNotEmpty(conditions)) {
        query.join("grade.lesson", "lesson")
        query.where(conditions)
      }
      query.where("grade.std.project = :project", project)
      query.where("grade.std.department in (:departments)", departments)
      query.where("grade.status = :status", Grade.Status.PUBLISHED)
    }
    query.orderBy(Order.parse(get("orderBy"))).limit(getPageLimit)
    query
  }

  def edit(): String = {
    courseGradeHelper.editGrade()
    forward()
  }

  def editGradeState(): String = {
    put("status", get("status"))
    val lesson = entityDao.get(classOf[Lesson], getLong("lessonId"))
    val gradeTypes = lessonGradeService.getCanInputGradeTypes(true)
    put("gradeTypes", gradeTypes)
    var gradeState = courseGradeService.getState(lesson)
    if (gradeState == null) {
      gradeState = Model.newInstance(classOf[CourseGradeState])
      gradeState.setLesson(lesson)
      gradeState.setScoreMarkStyle(Model.newInstance(classOf[ScoreMarkStyle], ScoreMarkStyle.PERCENT))
      entityDao.saveOrUpdate(gradeState)
    }
    for (`type` <- gradeTypes if null == gradeState.getState(`type`)) gradeState.updateStatus(`type`, 
      Grade.Status.NEW)
    put("markStyles", gradeRateService.getMarkStyles(gradeState.getLesson.getProject))
    put("gradeState", gradeState)
    put("lesson", lesson)
    val setting = settings.getSetting(lesson.getProject)
    val hasPercentIds = Collections.collect(setting.getGaElementTypes, new PropertyTransformer("id"))
    put("hasPercentIds", hasPercentIds)
    forward()
  }

  protected def getExportDatas(): Iterable[_] = {
    val kind = get("kind")
    if (Strings.isEmpty(kind) || kind == "noGradeTakes") {
      val takeIds = get("takeIds")
      if (Strings.isNotBlank(takeIds)) {
        return entityDao.get(classOf[CourseTake], Strings.splitToLong(takeIds))
      }
      return entityDao.search(buildNoGradeCourseTakeQuery().limit(null))
    } else if (kind == "unPassedGrades") {
      val gradeIds = get("gradeIds")
      if (Strings.isNotBlank(gradeIds)) {
        return entityDao.get(classOf[CourseGrade], Strings.splitToLong(gradeIds))
      }
      return entityDao.search(buildUnPassedGradeQuery().limit(null))
    }
    super.getExportDatas
  }

  protected override def indexSetting() {
    val semester = getAttribute("semester").asInstanceOf[Semester]
    val project = getProject
    val projects = Collections.newBuffer[Any](project)
    val departs = getDeparts
    if (semester != null) {
      put("courseTypes", lessonService.courseTypesOfSemester(projects, departs, semester))
      put("teachDepartList", lessonService.teachDepartsOfSemester(projects, departs, semester))
      put("departmentList", lessonService.teachDepartsOfSemester(projects, departs, semester))
    }
    val gradeTypes = settings.getSetting(project).getPublishableTypes
    gradeTypes.remove(new GradeType(GradeTypeConstants.FINAL_ID))
    val builder = OqlBuilder.from(classOf[GradeType], "gradeType").where("gradeType in (:types)", gradeTypes)
      .orderBy("gradeType.code")
      .cacheable()
    put("setting", settings.getSetting(getProject))
    put("gradeTypes", entityDao.search(builder))
  }

  def info(): String = {
    var gradeTypeId = getInt("statusGradeTypeId")
    if (null == gradeTypeId) gradeTypeId = GradeTypeConstants.FINAL_ID
    put("gradeType", entityDao.get(classOf[GradeType], gradeTypeId))
    teachClassGradeHelper.info(entityDao.get(classOf[Lesson], getLong("lessonId")))
    forward()
  }

  def noGradeTakes(): String = {
    put("noGradeTakes", entityDao.search(buildNoGradeCourseTakeQuery()))
    forward()
  }

  def remove(): String = {
    val msg = courseGradeHelper.removeStdGrade()
    if (Strings.isEmpty(msg)) {
      redirect("info", "info.delete.success", "&lessonId=" + getLong("lessonId"))
    } else {
      forwardError(msg)
    }
  }

  def removeGrade(): String = {
    val msg = courseGradeHelper.removeLessonGrade(getUserId)
    if (Strings.isEmpty(msg)) {
      logHelper.info("delete grade")
      redirect("search", "info.delete.success")
    } else {
      forwardError(msg)
    }
  }

  def report(): String = {
    forward(new Action(classOf[ReportAction], "report"))
  }

  def save(): String = {
    courseGradeHelper.saveGrade(entityDao.get(classOf[User], getUserId))
    val courseGradeId = getLong("courseGrade.id")
    val courseGrade = entityDao.get(classOf[CourseGrade], courseGradeId)
    redirect("info", "info.save.success", "&lessonId=" + courseGrade.getLesson.id)
  }

  def saveGradeState(): String = {
    val gradeState = populateEntity(classOf[CourseGradeState], "gradeState")
    val setting = settings.getSetting(gradeState.getLesson.getProject)
    val removed = Collections.newBuffer[Any]
    var finalStatus = 0
    var finalStyle: ScoreMarkStyle = null
    var finalPrecision = 0
    for (gradeType <- entityDao.getAll(classOf[GradeType])) {
      val egs = populateEntity(classOf[ExamGradeState], "state" + gradeType.id)
      val stateId = get("state" + gradeType.id + ".id")
      if (null == stateId) {
        if (null != gradeState.getState(gradeType)) removed.add(gradeState.getState(gradeType))
        //continue
      } else {
        if (egs.isTransient) {
          egs.setGradeType(gradeType)
          gradeState.getStates.add(egs)
        }
        egs.setGradeState(gradeState)
      }
      if (null != egs.getPercent) egs.setPercent(egs.getPercent.floatValue() / 100F)
      if (setting.getFinalCandinateTypes.contains(gradeType)) {
        if (egs.getStatus > finalStatus) finalStatus = egs.getStatus
      }
      if (gradeType.id == GradeTypeConstants.GA_ID) {
        finalStyle = egs.getScoreMarkStyle
        finalPrecision = egs.getPrecision
      }
    }
    gradeState.setStatus(finalStatus)
    if (null != finalStyle) gradeState.setScoreMarkStyle(finalStyle)
    gradeState.setPrecision(finalPrecision)
    gradeState.getStates.removeAll(removed)
    courseGradeService.recalculate(gradeState)
    redirect("search", "info.save.success")
  }

  def search(): String = {
    val project = getProject
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    var gradeTypeId = getInt("statusGradeTypeId")
    if (null == gradeTypeId) gradeTypeId = GradeTypeConstants.FINAL_ID
    var status = getInt("status")
    put("status", "status")
    var semesterId = getInt("lesson.semester.id")
    if (semesterId == null) {
      val semester = putSemester(null)
      semesterId = semester.id
      query.where("lesson.semester.id = :semesterId", semesterId)
    }
    if (null == status) status = 0
    populateConditions(query)
    val grade = get("fake_grade")
    if (Strings.isNotBlank(grade)) {
      query.where("exists(from lesson.teachClass.limitGroups gradeLgp join gradeLgp.items gradeLit " + 
        "where gradeLit.meta.id=:grade_metaId " + 
        "and gradeLit.operator in(:grade_in_operators) " + 
        "and gradeLit.content like :grade_content)", LessonLimitMeta.Grade.getMetaId, Array(Operator.IN, Operator.Equals), 
        "%" + grade + "%")
    }
    query.where("lesson.teachDepart in (:departs)", getDeparts)
    query.where("lesson.project=:p", project)
    if (0 == status) {
      if (gradeTypeId == GradeTypeConstants.FINAL_ID) {
        query.where("not exists (from " + classOf[CourseGradeState].getName + 
          " cgs where cgs.status >0 and cgs.lesson = lesson)")
      } else {
        val additional = makeupStdStrategy.getLessonCondition(gradeTypeId)
        query.where("not exists (from " + classOf[CourseGradeState].getName + 
          " cgs join cgs.states as examgs where examgs.gradeType.id = :gradeTypeId and  examgs.status>0 and cgs.lesson = lesson) " + 
          additional, gradeTypeId)
      }
    } else {
      if (gradeTypeId == GradeTypeConstants.FINAL_ID) {
        query.where("exists (from " + classOf[CourseGradeState].getName + 
          " cgs where cgs.status = " + 
          status + 
          " and cgs.lesson = lesson)")
      } else {
        query.where("exists (from " + classOf[CourseGradeState].getName + 
          " cgs join cgs.states examgs where examgs.gradeType.id = :gradeTypeId and examgs.status =  " + 
          status + 
          " and cgs.lesson = lesson)", gradeTypeId)
      }
    }
    val teacherQueryConditions = QueryHelper.extractConditions(classOf[Teacher], "teacher", null)
    if (Collections.isNotEmpty(teacherQueryConditions)) {
      query.join("lesson.teachers", "teacher").where(teacherQueryConditions)
    }
    val personPercentFlag = getBoolean("personPercentFlag")
    if (null != personPercentFlag) {
      if (true == personPercentFlag) {
        query.where("exists (from " + classOf[CourseGradeState].getName + 
          " cgs1 where cgs1.auditStatus is not null and cgs1.lesson = lesson)")
      } else {
        query.where("not exists (from " + classOf[CourseGradeState].getName + 
          " cgs1 where cgs1.auditStatus is not null and cgs1.lesson = lesson)")
      }
    }
    query.limit(getPageLimit)
    val orderBy = get("orderBy")
    query.orderBy(Order.parse(if (Strings.isEmpty(orderBy)) "lesson.no" else orderBy))
    val lessons = entityDao.search(query)
    put("lessons", lessons)
    val courseGradeStateMap = Collections.newMap[Any]
    for (lesson <- lessons) {
      val gradeState = courseGradeService.getState(lesson)
      if (null != gradeState) courseGradeStateMap.put(lesson, gradeState)
    }
    put("courseGradeStateMap", courseGradeStateMap)
    put("gradeTypes", lessonGradeService.getCanInputGradeTypes(false))
    put("courseGradeTypes", entityDao.get(classOf[GradeType], Array(GradeTypeConstants.GA_ID, GradeTypeConstants.FINAL_ID)))
    put("gradeType", entityDao.get(classOf[GradeType], gradeTypeId))
    put("printableTypes", entityDao.get(classOf[GradeType], Array(GradeTypeConstants.MAKEUP_ID)))
    put("validateToken", getUsername.hashCode)
    forward()
  }

  def searchUsers(): String = {
    var codeOrNames = get("term")
    val query = OqlBuilder.from(classOf[User], "user")
    query.join("user.categories", "category")
    query.where("category.id = :categoryId1 or category.id = :categoryId2", EamsUserCategory.TEACHER_USER, 
      EamsUserCategory.MANAGER_USER)
    if (Strings.isNotEmpty(codeOrNames)) {
      codeOrNames = codeOrNames.replace('，', ',').replaceAll(",+", ",")
      val conds = Strings.split(codeOrNames)
      if (null != conds && conds.length != 0) {
        val sb = new StringBuilder()
        sb.append("(\n")
        for (i <- 0 until conds.length) {
          val like = "'%" + conds(i) + "%'"
          if (Strings.isEmpty(like)) {
            //continue
          }
          if (i != 0) {
            sb.append("\n or ")
          }
          sb.append("user.name like ").append(like).append(" or user.fullname like ")
            .append(like)
        }
        sb.append("\n)")
        query.where(sb.toString)
      }
    }
    val now = new Date()
    query.where(":now1 >= user.effectiveAt and (user.invalidAt is null or :now2 <= user.invalidAt)", 
      now, now)
      .orderBy("user.name")
    query.limit(getPageLimit)
    put("users", entityDao.search(query))
    forward("userJSON")
  }

  def stat(): String = {
    forward(new Action(classOf[ReportAction], "stat"))
  }

  def unPassedGrades(): String = {
    put("unPassedGrades", entityDao.search(buildUnPassedGradeQuery()))
    forward()
  }
}
