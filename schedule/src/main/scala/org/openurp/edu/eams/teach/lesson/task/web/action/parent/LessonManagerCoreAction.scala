package org.openurp.edu.eams.teach.lesson.task.web.action.parent

import org.beangle.commons.web.util.RequestUtils.encodeAttachName
import java.io.IOException

import java.util.Date


import java.util.LinkedHashMap



import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.lang3.ArrayUtils
import org.apache.struts2.ServletActionContext
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings

import org.beangle.commons.transfer.TransferResult
import org.beangle.commons.transfer.excel.ExcelTemplateWriter
import org.beangle.commons.transfer.exporter.Context
import org.beangle.commons.transfer.exporter.DefaultPropertyExtractor
import org.beangle.commons.transfer.exporter.Exporter
import org.beangle.commons.transfer.exporter.TemplateExporter
import org.beangle.commons.transfer.io.TransferFormat
import org.beangle.commons.web.util.RequestUtils
import org.beangle.ems.config.model.PropertyConfigItemBean
import org.beangle.struts2.convention.route.Action
import org.openurp.base.Campus
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.code.person.Gender
import org.openurp.edu.eams.base.code.school.RoomType
import org.openurp.edu.eams.base.model.WeekState
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.core.service.AdminclassService
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.system.doc.service.DocPath
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.code.industry.ExamMode
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.eams.teach.code.industry.TeachLangType
import org.openurp.edu.eams.teach.code.school.CourseHourType
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.exam.ExamTurn
import org.openurp.edu.eams.teach.exam.service.ExamYearWeekTimeUtil
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.dao.LessonSeqNoGenerator
import org.openurp.edu.eams.teach.lesson.model.CourseLimitMetaBean
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.model.LessonTagBean
import org.openurp.edu.eams.teach.lesson.model.NormalClassBean
import org.openurp.edu.teach.lesson.model.TeachClassBean
import org.openurp.edu.eams.teach.lesson.service.CourseLimitGroupBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.teach.lesson.service.LessonOperateViolation
import org.openurp.edu.eams.teach.lesson.service.TaskCopyParams
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitItemContentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitItemContentProviderFactory
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnumProvider
import org.openurp.edu.eams.teach.lesson.task.service.LessonCollegeSwitchService
import org.openurp.edu.eams.teach.lesson.task.service.LessonMergeSplitService
import org.openurp.edu.eams.teach.lesson.task.service.helper.LessonExamArrangeHelper
import org.openurp.edu.eams.teach.lesson.task.splitter.AbstractTeachClassSplitter
import org.openurp.edu.eams.teach.lesson.task.splitter.AdminclassGroupMode
import org.openurp.edu.eams.teach.lesson.task.splitter.NumberMode
import org.openurp.edu.eams.teach.lesson.task.util.ProjectUtils
import org.openurp.edu.eams.teach.lesson.task.web.action.TeachTaskSearchAction
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.eams.web.helper.BaseInfoSearchHelper
import LessonManagerCoreAction._



object LessonManagerCoreAction {

  val cascadeList = CollectUtils.newArrayList(CourseLimitMetaEnum.ADMINCLASS.getMetaId, CourseLimitMetaEnum.DIRECTION.getMetaId, 
    CourseLimitMetaEnum.MAJOR.getMetaId, CourseLimitMetaEnum.PROGRAM.getMetaId)
}

abstract class LessonManagerCoreAction extends TeachTaskSearchAction {

  protected var adminClassService: AdminclassService = _

  protected var baseInfoSearchHelper: BaseInfoSearchHelper = _

  protected var lessonDao: LessonDao = _

  protected var lessonLogHelper: LessonLogHelper = _

  protected var lessonMergeSplitService: LessonMergeSplitService = _

  protected var timeSettingService: TimeSettingService = _

  protected var lessonCollegeSwitchService: LessonCollegeSwitchService = _

  protected var lessonExamArrangeHelper: LessonExamArrangeHelper = _

  protected var courseLimitMetaEnumProvider: CourseLimitMetaEnumProvider = _

  protected var courseLimitItemContentProviderFactory: CourseLimitItemContentProviderFactory = _

  protected var teachClassNameStrategy: TeachClassNameStrategy = _

  def operateViolationCheck(lesson: Lesson): LessonOperateViolation

  def operateViolationCheck(lessons: List[Lesson]): LessonOperateViolation

  def copyViolationCheck(lesson: Lesson, semester: Semester): LessonOperateViolation

  def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    val semester = getAttribute("semester").asInstanceOf[Semester]
    val project = getProject
    val teachDeparts = CollectUtils.newArrayList()
    val departs = CollectUtils.newHashSet(ProjectUtils.getTeachDeparts(project))
    for (department <- getDeparts if departs.contains(department)) {
      teachDeparts.add(department)
    }
    if (semester != null) {
      put("attendDeparts", getCollegeOfDeparts)
      put("teachDeparts", teachDeparts)
      put("courseTypes", lessonService.courseTypesOfSemester(Collections.singletonList(project), teachDeparts, 
        semester))
      put("weeks", WeekDays.All)
    }
    put("departs", getDeparts)
    put("teacherDeparts", getDeparts)
    addBaseCode("langTypes", classOf[TeachLangType])
    put("campuses", project.getCampuses)
    put("lessonCollegeSwitchStatus", lessonCollegeSwitchService.status(semester.id, getProject.id))
    forward()
  }

  def calcWeekState(): String = {
    val courseId = getLong("courseId")
    val weekState = get("weekState")
    val state = WeekStates.build(weekState)
    val course = entityDao.get(classOf[Course], courseId)
    val weekHour = CourseScheduleBean.calcWeekHour(course.getPeriod, state.getWeeks)
    getResponse.getWriter.append("ws = {weekHour:" + weekHour + ",weeks:" + state.getWeeks + 
      "}")
    null
  }

  def batch(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    if (notInDataRealm(lessons)) {
      return forwardError("error.depart.dataRealm.insufficient")
    }
    operateViolationCheck(lessons) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.batchedit")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.batchedit")
      case _ => //break
    }
    put("lessons", lessons)
    put("tags", entityDao.getAll(classOf[LessonTag]))
    put("classroomTypes", baseCodeService.getCodes(classOf[RoomType]))
    put("teachDeparts", departmentService.getTeachDeparts)
    put("langTypes", baseCodeService.getCodes(classOf[TeachLangType]))
    put("guaPaiTagId", LessonTag.PredefinedTags.GUAPAI.id)
    put("canxuanTagId", LessonTag.PredefinedTags.ELECTABLE.id)
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    put("examModes", baseCodeService.getCodes(classOf[ExamMode]))
    addBaseInfo("campuses", classOf[Campus])
    put("weekStates", new WeekStates())
    put("_ARRANGED", CourseStatusEnum.ARRANGED)
    forward()
  }

  def batchSettingTeachClass(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    operateViolationCheck(lessons) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.batchedit")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.batchedit")
      case _ => //break
    }
    put("lessons", lessons)
    val genderMap = new HashMap[String, Gender]()
    for (lesson <- lessons) {
      genderMap.put(lesson.id.toString, courseLimitService.extractGender(lesson.getTeachClass))
    }
    put("genderMap", genderMap)
    put("lessonIds", get("lessonIds"))
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    addBaseCode("genderList", classOf[Gender])
    forward("/com/ekingstar/eams/teach/lesson/task/web/action/lessonManagerCore/batchSettingTeachClass")
  }

  def saveBatchSettingTeachClass(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    operateViolationCheck(lessons) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.batchsave")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.batchsave")
      case _ => //break
    }
    for (lesson <- lessons) {
      val teachClassName = get("fake" + lesson.id + ".teachClass.name")
      lesson.getTeachClass.setName(teachClassName)
      val grade = get("fake" + lesson.id + ".teachClass.grade")
      lesson.getTeachClass.setGrade(grade)
      val genderId = getInt("fake" + lesson.id + ".gender.id")
      val courseLimitBuilder = courseLimitService.builder(lesson.getTeachClass)
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.GENDER.getMetaId))
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.GRADE.getMetaId))
      if (null != genderId) {
        courseLimitBuilder.in(entityDao.get(classOf[Gender], genderId))
      }
      if (Strings.isNotEmpty(grade)) {
        courseLimitBuilder.inGrades(Strings.split(grade))
      }
      val emptyGroups = new ArrayList[CourseLimitGroup]()
      for (group <- lesson.getTeachClass.getLimitGroups if CollectUtils.isEmpty(group.getItems)) {
        emptyGroups.add(group)
      }
      lesson.getTeachClass.getLimitGroups.removeAll(emptyGroups)
      val limitCount = getInt("fake" + lesson.id + ".teachClass.limitCount")
      lesson.getTeachClass.setLimitCount(limitCount)
      val autoName = getBoolean("fake" + lesson.id + ".autoname")
      if (true == autoName) {
        teachClassNameStrategy.autoName(lesson.getTeachClass)
      }
      entityDao.saveOrUpdate(lesson)
      lessonLogHelper.log(LessonLogBuilder.update(lesson, null))
    }
    redirect("search", "info.save.success", get("queryParams"))
  }

  def batchSave(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    operateViolationCheck(lessons) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.batchsave")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.batchsave")
      case _ => //break
    }
    for (lesson <- lessons) {
      val courseId = getLong("fake" + lesson.id + ".course")
      if (courseId != null) {
        lesson.setCourse(entityDao.get(classOf[Course], courseId))
      }
      val courseTypeId = getInt("fake" + lesson.id + ".courseType.id")
      if (null != courseTypeId) {
        lesson.setCourseType(entityDao.get(classOf[CourseType], courseTypeId))
      }
      val teachDepartId = getInt("fake" + lesson.id + ".teachDepart.id")
      if (null != teachDepartId) {
        lesson.setTeachDepart(entityDao.get(classOf[Department], teachDepartId))
      }
      val examModeId = getInt("fake" + lesson.id + ".examMode.id")
      if (examModeId == null) {
        lesson.setExamMode(null)
      } else {
        lesson.setExamMode(entityDao.get(classOf[ExamMode], examModeId))
      }
      val teacherIds = ids("fake" + lesson.id + ".teacher", classOf[Long])
      if (teacherIds == null || teacherIds.length == 0) {
        lessonService.fillTeachers(Array.ofDim[Long](0), lesson)
      } else {
        lessonService.fillTeachers(teacherIds, lesson)
      }
      val startWeek = getInt("lesson" + lesson.id + ".courseSchedule.startWeek")
      val endWeek = getInt("lesson" + lesson.id + ".courseSchedule.endWeek")
      if (null != startWeek && null != endWeek) {
        lesson.getCourseSchedule.setWeekState(WeekStates.build(startWeek + "-" + endWeek))
      } else {
        lesson.getCourseSchedule.setWeekState(WeekStates.build(get("lesson" + lesson.id + ".courseSchedule.weekState")))
      }
      val roomTypeId = getInt("fake" + lesson.id + ".roomType.id")
      if (roomTypeId == null) {
        lesson.getCourseSchedule.setRoomType(null)
      } else {
        lesson.getCourseSchedule.setRoomType(entityDao.get(classOf[RoomType], roomTypeId))
      }
      val langId = getInt("fake" + lesson.id + ".lang.id")
      if (langId == null) {
        lesson.setLangType(null)
      } else {
        lesson.setLangType(entityDao.get(classOf[TeachLangType], langId))
      }
      lesson.getTags.clear()
      val tags = entityDao.getAll(classOf[LessonTag])
      for (tag <- tags if true == getBoolean("fake" + lesson.id + ".guapai" + tag.id)) {
        lesson.getTags.add(tag)
      }
      val campusId = getInt("fake" + lesson.id + ".campus.id")
      if (campusId == null) {
        lesson.setCampus(null)
      } else {
        lesson.setCampus(entityDao.get(classOf[Campus], campusId))
      }
      val remark = get("fake" + lesson.id + ".remark")
      lesson.setRemark(remark)
      val limitCount = getInt("fake" + lesson.id + ".teachClass.limitCount")
      if (null != limitCount) {
        lesson.getTeachClass.setLimitCount(limitCount)
      }
      entityDao.saveOrUpdate(lesson)
      lessonLogHelper.log(LessonLogBuilder.update(lesson, null))
    }
    redirect("search", "info.save.success")
  }

  def batchAdd(): String = {
    val courseIds = Strings.splitToLong(get("courseIds"))
    val semester = entityDao.get(classOf[Semester], getInt("lesson.semester.id"))
    val project = entityDao.get(classOf[Project], getInt("lesson.project.id"))
    val defaultType = baseCodeService.getCodes(classOf[CourseType]).get(0)
    for (courseId <- courseIds) {
      val course = entityDao.get(classOf[Course], courseId)
      val lesson = populate(classOf[Lesson], "lesson")
      lesson.setTeachDepart(course.department)
      lesson.setProject(project)
      lesson.setCourse(course)
      lesson.setExamMode(course.getExamMode)
      lesson.setCourseSchedule(new CourseScheduleBean())
      lesson.getCourseSchedule.setLesson(lesson)
      lesson.getCourseSchedule.setWeekState(WeekStates.build(1 + "-" + 
        (if (course.getWeeks == null) 1 else course.getWeeks)))
      lesson.getCourseSchedule.setPeriod(course.getPeriod)
      if (course.getCourseType == null) {
        lesson.setCourseType(defaultType)
      } else {
        lesson.setCourseType(course.getCourseType)
      }
      lesson.setSemester(semester)
      lesson.setTeachClass(new TeachClassBean())
      teachClassNameStrategy.autoName(lesson.getTeachClass)
      operateViolationCheck(lesson) match {
        case PERMIT_VIOLATION => return forwardError("lesson.college.violation.batchsave")
        case _ => //break
      }
      lesson.setAuditStatus(CommonAuditState.UNSUBMITTED)
      lessonDao.saveOrUpdate(lesson)
      lessonLogHelper.log(LessonLogBuilder.create(lesson, null))
    }
    redirect("search", "info.save.success")
  }

  def remove(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    if (notInDataRealm(lessons)) {
      return forwardError("error.depart.dataRealm.insufficient")
    }
    operateViolationCheck(lessons) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.remove")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.remove")
      case _ => //break
    }
    for (lesson <- lessons) {
      lessonDao.remove(lesson)
      lessonLogHelper.log(LessonLogBuilder.delete(lesson, null))
    }
    redirect("search", "info.delete.success")
  }

  private def inDataRealm(lessons: List[Lesson]): Boolean = {
    lessons.find(task => !getProjects.contains(task.getProject) || !getDeparts.contains(task.getTeachDepart))
      .map(_ => false)
      .getOrElse(true)
  }

  private def notInDataRealm(lessons: List[Lesson]): Boolean = !inDataRealm(lessons)

  def checkScheduledPeriod(): String = {
    val query = lessonSearchHelper.buildQuery().orderBy("lesson.no")
      .where("lesson.courseSchedule.endWeek - lesson.courseSchedule.startWeek + 1 <> lesson.course.weeks")
    val lessons = entityDao.search(query)
    put("lessons", lessons)
    forward()
  }

  def autoAdjustSchedule(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    operateViolationCheck(lessons) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.batchsave")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.batchsave")
      case _ => //break
    }
    for (lesson <- lessons) {
      val startWeek = lesson.getCourseSchedule.getStartWeek
      val endWeek = (if (lesson.getCourse.getWeeks == null) 1 else lesson.getCourse.getWeeks) + 
        startWeek - 
        1
      lesson.getCourseSchedule.setWeekState(WeekStates.build(startWeek + "-" + endWeek))
    }
    entityDao.saveOrUpdate(lessons)
    redirect("checkScheduledPeriod", "info.action.success", get("params"))
  }

  def copy(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    if (null == lessonIds || lessonIds.length == 0) {
      return forwardError("error.model.id.needed")
    }
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    val semester = entityDao.get(classOf[Semester], getInt("semester.id"))
    val params = populate(classOf[TaskCopyParams], "params")
    params.setSemester(semester)
    for (lesson <- lessons) copyViolationCheck(lesson, semester) match {
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.copy")
      case _ => //break
    }
    for (i <- 0 until params.getCopyCount) {
      lessonService.copy(lessons, params)
    }
    redirect("search", "任务复制成功")
  }

  def copySetting(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    if (null == lessonIds || lessonIds.length == 0) {
      return forwardError("error.model.id.needed")
    }
    put("project", entityDao.get(classOf[Project], getInt("lesson.project.id")))
    put("semester", entityDao.get(classOf[Semester], getInt("lesson.semester.id")))
    put("lessons", entityDao.get(classOf[Lesson], lessonIds))
    forward()
  }

  def courses(): String = {
    val limit = getPageLimit
    put("project", entityDao.get(classOf[Project], getInt("course.project.id")))
    if (getBool("batch")) {
      put("courses", entityDao.search(buildCourseQuery().where("course.department is not null")
        .limit(limit)))
    } else {
      put("courses", entityDao.search(buildCourseQuery().limit(limit)))
    }
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward()
  }

  def buildCourseQuery(): OqlBuilder[Course] = {
    val builder = OqlBuilder.from(classOf[Course], "course")
    populateConditions(builder)
    builder.limit(getPageLimit)
    var orderByPras = get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "course.code"
    }
    builder.orderBy(orderByPras)
    builder
  }

  def declarationForm(): String = {
    val query = lessonSearchHelper.buildQuery()
    query.limit(null)
    put("tasks", entityDao.search(query))
    put("perPageRows", getLong("perPageRows"))
    forward()
  }

  def exportDeclarationForm(): String = {
    val query = lessonSearchHelper.buildQuery()
    query.limit(null)
    val format = TransferFormat.Xls
    var fileName = get("fileName")
    var template = get("template")
    if (Strings.isEmpty(fileName)) {
      fileName = "exportResult"
    }
    val context = new Context()
    context.getDatas.put("format", format)
    context.getDatas.put("exportFile", fileName)
    val defaultPath = ServletActionContext.getServletContext.getRealPath(DocPath.fileDirectory)
    val docPath = DocPath.getRealPath(getConfig, DocPath.TEMPLATE_DOWNLOAD, defaultPath)
    template = docPath + template
    context.getDatas.put("templatePath", template)
    context.getDatas.put("items", entityDao.search(query))
    val request = getRequest
    val response = getResponse
    val exporter = new TemplateExporter()
    exporter.setWriter(new ExcelTemplateWriter(response.getOutputStream))
    response.setContentType("application/vnd.ms-excel;charset=GBK")
    response.setHeader("Content-Disposition", "attachment;filename=" + 
      RequestUtils.encodeAttachName(request, context.getDatas.get("exportFile").toString) + 
      ".xls")
    exporter.setContext(context)
    exporter.transfer(new TransferResult())
    null
  }

  def edit(): String = {
    val project = getProject
    val semester = putSemester(project)
    val lessonId = getLong("lesson.id")
    val courseId = getLong("lesson.course.id")
    if (null == courseId && null == lessonId) {
      return forwardError("error.model.id.needed")
    }
    var lesson: Lesson = null
    val courseLimitMetaEnumPairs = courseLimitMetaEnumProvider.getCourseLimitMetaPairs
    val courseMetaIds = courseLimitMetaEnumPairs.getLeft
    val courseLimitMetaEnums = courseLimitMetaEnumPairs.getRight
    var limitItems = Collections.emptyList()
    if (null == lessonId) {
      val course = entityDao.get(classOf[Course], courseId)
      lesson = populate(classOf[Lesson], "lesson")
      lesson.setTeachDepart(course.department)
      lesson.setProject(project)
      lesson.setCourse(course)
      lesson.setExamMode(course.getExamMode)
      lesson.setCourseSchedule(new CourseScheduleBean())
      lesson.getCourseSchedule.setLesson(lesson)
      lesson.getCourseSchedule.setPeriod(course.getPeriod)
      lesson.getCourseSchedule.setWeekState(WeekStates.build(1 + "-" + 
        (if (course.getWeeks == null) 1 else course.getWeeks)))
      lesson.setCourseType(course.getCourseType)
      lesson.setSemester(semester)
      lesson.setTeachClass(new TeachClassBean())
      lesson.getTeachClass.setName(course.getName)
      operateViolationCheck(lesson) match {
        case PERMIT_VIOLATION => return forwardError("lesson.college.violation.edit")
        case _ => //break
      }
    } else {
      lesson = entityDao.get(classOf[Lesson], lessonId)
      if (notInDataRealm(Collections.singletonList(lesson))) {
        return forwardError("error.dataRealm.insufficient")
      }
      operateViolationCheck(lesson) match {
        case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.edit")
        case PERMIT_VIOLATION => return forwardError("lesson.college.violation.edit")
        case _ => //break
      }
      val itemQuery = OqlBuilder.from(classOf[CourseLimitItem], "limitItem")
      itemQuery.where("limitItem.group.lesson.id=:lessonId", lessonId)
      itemQuery.where("limitItem.meta.id in(:metaIds)", courseMetaIds)
      itemQuery.orderBy("limitItem.group.id asc,limitItem.meta.id asc")
      limitItems = entityDao.search(itemQuery)
    }
    put("courseLimitMetaEnums", courseLimitMetaEnums)
    val metaIdEnums = new HashMap[Long, CourseLimitMetaEnum]()
    for (courseLimitMetaEnum <- courseLimitMetaEnums) {
      metaIdEnums.put(courseLimitMetaEnum.getMetaId, courseLimitMetaEnum)
    }
    val groupItems = new LinkedHashMap[CourseLimitGroup, List[CourseLimitItem]]()
    val limitItemContents = new HashMap[Long, Map[String, String]]()
    for (limitItem <- limitItems) {
      val courseLimitMetaEnum = metaIdEnums.get(limitItem.getMeta.id)
      if (null != courseLimitMetaEnum) {
        val group = limitItem.getGroup
        var items = groupItems.get(group)
        if (null == items) {
          items = new ArrayList[CourseLimitItem]()
          groupItems.put(group, items)
        }
        val provider = courseLimitItemContentProviderFactory.getProvider(courseLimitMetaEnum)
        limitItemContents.put(limitItem.id, provider.getContentIdTitleMap(limitItem.getContent))
        items.add(limitItem)
      }
    }
    put("groupItems", groupItems)
    put("limitItemContents", limitItemContents)
    put("limitMetas", entityDao.get(classOf[CourseLimitMeta], metaIdEnums.keySet))
    put("electedGroupIds", getElectedLimitGroupIds(lesson))
    put("tags", entityDao.getAll(classOf[LessonTag]))
    put("isAutoName", courseLimitService.isAutoName(lesson))
    if (isLessonExamArrange) {
      put("isLessonExamArrange", true)
      put("weeks", WeekDays.All)
      if (lesson.id != null) {
        val activity = lessonExamArrangeHelper.getExamActivityByLesson(lesson)
        if (activity != null) {
          put("exam_weeks", ExamYearWeekTimeUtil.getTeachWeekOfYear(lesson.getSemester, activity.getStartAt))
          put("exam_weekDay", ExamYearWeekTimeUtil.getWeekDayByDate(activity.getStartAt))
          put("exam_turn", lessonExamArrangeHelper.getExamTurnByActivity(activity))
          put("activity", activity)
        }
      }
    }
    val query = OqlBuilder.from(classOf[ExamTurn], "turn")
    query.orderBy("turn.beginTime,turn.endTime")
    put("examTurns", entityDao.search(query))
    put("normalClass", new NormalClassBean())
    put("lesson", lesson)
    put("guaPaiTagId", LessonTag.PredefinedTags.GUAPAI.id)
    addBaseCode("courseTypeList", classOf[CourseType])
    addBaseCode("classroomTypeList", classOf[RoomType])
    addBaseInfo("campusList", classOf[Campus])
    put("examModes", baseCodeService.getCodes(classOf[ExamMode]))
    put("teacherDeparts", project.departments)
    put("teachDepartList", ProjectUtils.getTeachDeparts(project))
    addBaseCode("langTypes", classOf[TeachLangType])
    put("weekStates", new WeekStates())
    forward()
  }

  private def getElectedLimitGroupIds(lesson: Lesson): Set[Long] = {
    val groupIds = CollectUtils.newHashSet()
    val takes = lesson.getTeachClass.getCourseTakes
    for (courseTake <- takes if courseTake.getLimitGroup != null) {
      groupIds.add(courseTake.getLimitGroup.id)
    }
    groupIds
  }

  private def buildCascadeContentMap(cascadeContents: String): Map[Long, String] = {
    val cascadeContentMap = CollectUtils.newHashMap()
    val cascadeArray = Strings.split(cascadeContents, ";")
    for (i <- 0 until cascadeArray.length) {
      val cascade = Strings.split(cascadeArray(i), ":")
      cascadeContentMap.put(java.lang.Long.valueOf(cascade(0)), cascade(1))
    }
    cascadeContentMap
  }

  def getLimitDatas(): String = {
    val `type` = get("type")
    val term = get("term")
    val queryType = get("queryType")
    var result = "entity"
    if ("meta" == `type`) {
      val ids = getAll("content", classOf[Long])
      val queryBuilder = OqlBuilder.from(classOf[CourseLimitMeta], "limitMeta")
      queryBuilder.where("limitMeta.name like :term", "%" + term + "%")
      if (!Arrays.isEmpty(ids)) {
        queryBuilder.where("limitMeta.id not in (:ids)", ids)
      }
      queryBuilder.orderBy("id")
      put("entities", entityDao.search(queryBuilder))
    } else {
      val metaId = getLong("metaId")
      val content = get("content")
      val provider = courseLimitItemContentProviderFactory.getProvider(metaId)
      if ("byContent" == queryType) {
        put("entities", provider.getContents(content).values)
      } else {
        if (cascadeList.contains(metaId)) {
          put("entities", provider.getCascadeContents(content, term, getPageLimit, buildCascadeContentMap(get("cascadeContents"))))
        } else {
          put("entities", provider.getOtherContents(content, term, getPageLimit))
        }
      }
      if (!classOf[Entity[_]].isAssignableFrom(provider.getMetaEnum.getContentType)) {
        result = provider.getMetaEnum.toString.toLowerCase()
      }
    }
    forward("limitDatas/" + result)
  }

  def exportSetting(): String = forward()

  def exportStdList(): String = {
    val lessonIds = getLongIds("lesson")
    if (ArrayUtils.isEmpty(lessonIds)) {
      return forwardError("error.model.id.needed")
    }
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    query.where("take.lesson.id in (:lessonIds)", lessonIds)
      .orderBy("take.lesson.no")
      .orderBy("take.std.code")
    var fileName = get("fileName")
    if (Strings.isEmpty(fileName)) {
      fileName = "exportResult"
    }
    val context = new Context()
    context.put("extractor", new DefaultPropertyExtractor())
    context.put("format", TransferFormat.Xls)
    context.put("exportFile", fileName)
    context.put(Context.KEYS, get("attrs"))
    context.put(Context.TITLES, get("attrNames"))
    context.put("items", entityDao.search(query))
    val response = ServletActionContext.getResponse
    val exporter = buildExporter(TransferFormat.Xls, context)
    exporter.getWriter.setOutputStream(response.getOutputStream)
    response.setContentType("application/vnd.ms-excel;charset=GBK")
    response.setHeader("Content-Disposition", "attachment;filename=" + 
      encodeAttachName(ServletActionContext.getRequest, fileName + ".xls"))
    exporter.setContext(context)
    exporter.transfer(new TransferResult())
    null
  }

  def printStdList(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    if (null == lessonIds || lessonIds.length == 0) {
      return forwardError("error.model.id.needed")
    }
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    val courseTakes = new HashMap[String, List[CourseTake]]()
    for (task <- lessons) {
      val myCourseTakes = new ArrayList[CourseTake]()
      myCourseTakes.addAll(task.getTeachClass.getCourseTakes)
      courseTakes.put(task.id.toString, myCourseTakes)
    }
    put("lessons", lessons)
    put("courseTakes", courseTakes)

    forward()
  }

  def printStdListPrepare(taskIds: String) {
  }

  def printNotifications(): String = {
    val teachTaskIds = get("lessonIds")
    if (Strings.isEmpty(teachTaskIds)) {
      return forwardError("error.model.id.needed")
    }
    val taskQuery = OqlBuilder.from(classOf[Lesson], "task")
    taskQuery.join("left outer", "task.teachers", "teacher")
    taskQuery.where("task.id in (:ids)", Strings.splitToLong(teachTaskIds))
      .orderBy("task.teachDepart.name")
      .orderBy("teacher.name")
    val tasks = entityDao.search(taskQuery)
    val arrangeInfos = new HashMap[Long, String]()
    val firstTimes = new HashMap[Long, Date]()
    val digestor = CourseActivityDigestor.getInstance.setDelimeter("<br>")
    var format = get("format")
    if (Strings.isEmpty(format)) {
      format = ":teacher2:day:time :weeks:room(:district :building)"
    }
    val educationMap = new HashMap[String, List[Education]]()
    val stdTypeMap = new HashMap[String, List[StdType]]()
    for (lesson <- tasks) {
      if (arrangeInfos.get(lesson.id) == null) {
        arrangeInfos.put(lesson.id, digestor.digest(getTextResource, lesson, format))
      }
      val myCourseTakes = CollectUtils.newArrayList()
      myCourseTakes.addAll(lesson.getTeachClass.getCourseTakes)
      val educations = courseLimitService.extractEducations(lesson.getTeachClass)
      educationMap.put(lesson.id.toString, educations)
      val stdTypes = courseLimitService.extractStdTypes(lesson.getTeachClass)
      stdTypeMap.put(lesson.id.toString, stdTypes)
      firstTimes.put(lesson.id, YearWeekTimeUtil.buildFirstLessonDay(lesson))
    }
    put("tasks", tasks)
    put("arrangeInfos", arrangeInfos)
    put("firstTimes", firstTimes)
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    put("educationMap", educationMap)
    put("stdTypeMap", stdTypeMap)
    forward()
  }

  def printLessonList(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    if (lessonIds == null || lessonIds.length == 0) {
      return forwardError("error.model.id.needed")
    }
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.id in (:ids)", lessonIds).orderBy("lesson.course.code")
      .orderBy(get("orderBy"))
      .limit(null)
    val lessons = entityDao.search(query)
    put("semester", entityDao.get(classOf[Semester], getInt("lesson.semester.id")))
    put("lessons", lessons)
    forward()
  }

  def save(): String = {
    var lesson = populate(classOf[Lesson], "lesson")
    var update = false
    if (lesson.isPersisted) {
      lesson = entityDao.get(classOf[Lesson], lesson.id)
      populate(lesson, "lesson")
      update = true
    }
    fillTag(lesson)
    fillCourseLimit(lesson)
    val weekState = get("weekState")
    if (Strings.isNotBlank(weekState)) lesson.getCourseSchedule.setWeekState(WeekStates.build(weekState))
    lessonService.fillTeachers(Strings.splitToLong(get("fake.teachers")), lesson)
    operateViolationCheck(lesson) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.save")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.save")
      case _ => //break
    }
    lessonDao.saveOrUpdate(lesson)
    if (update) {
      lessonLogHelper.log(LessonLogBuilder.update(lesson, null))
    } else {
      lessonLogHelper.log(LessonLogBuilder.create(lesson, null))
    }
    if (isLessonExamArrange) {
      var weeks = getInt("exam_weeks")
      val weekDay = getInt("exam_weekDay")
      val turnId = getLong("exam_turnId")
      if (weeks == null) {
        weeks = lesson.getCourseSchedule.getEndWeek + 1
      }
      if (weekDay != null && turnId != null) {
        lessonExamArrangeHelper.buildExamActivity(lesson, ExamType.FINAL, weeks, weekDay, turnId)
      }
    }
    if ("manualArrange" == get("from")) {
      return redirect(new Action("manualArrange", "taskList", get("params")), "info.save.success")
    }
    redirect("search", "info.save.success", get("params"))
  }

  protected def fillTag(lesson: Lesson) {
    val tagIds = get("fake.guaPai")
    lesson.getTags.clear()
    val bl = getBoolean("fake.guaPai")
    if (bl != null) {
      if (bl) {
        lesson.getTags.add(entityDao.get(classOf[LessonTag], LessonTagBean.GUAPAI))
      } else {
        lesson.getTags.remove(entityDao.get(classOf[LessonTag], LessonTagBean.GUAPAI))
      }
    } else {
      if (Strings.isNotEmpty(tagIds)) {
        lesson.getTags.addAll(getModels(classOf[LessonTag], Strings.splitToLong(tagIds)))
      }
    }
  }

  protected def fillCourseLimit(lesson: Lesson) {
    val teachClass = lesson.getTeachClass
    val electedLimitGroupIds = getElectedLimitGroupIds(lesson)
    val limitGroups = teachClass.getLimitGroups
    val toRemove = CollectUtils.newArrayList()
    for (courseLimitGroup <- limitGroups if !electedLimitGroupIds.contains(courseLimitGroup.id)) {
      toRemove.add(courseLimitGroup)
    }
    teachClass.getLimitGroups.removeAll(toRemove)
    val groupShortnames = getAll("groupKey", classOf[String])
    val groups = new HashMap[String, CourseLimitGroup]()
    if (!Arrays.isEmpty(groupShortnames)) {
      for (shortname <- groupShortnames) {
        val groupId = getLong(shortname + ".groupId")
        var group: CourseLimitGroup = null
        if (electedLimitGroupIds.contains(groupId)) {
          group = entityDao.get(classOf[CourseLimitGroup], groupId)
        } else {
          group = Model.newInstance(classOf[CourseLimitGroup])
          group.setLesson(lesson)
          teachClass.getLimitGroups.add(group)
          groups.put(shortname, group)
        }
        group.setMaxCount(getInt(shortname + "_limitCount"))
      }
    }
    val itemShortnames = getAll("itemKey", classOf[String])
    if (!Arrays.isEmpty(itemShortnames)) {
      for (shortname <- itemShortnames) {
        val group = groups.get(Strings.substringBeforeLast(shortname, "_"))
        if (null == group) {
          //continue
        }
        val item = populateEntity(classOf[CourseLimitItem], shortname)
        item.setGroup(group)
        group.getItems.add(item)
      }
    }
    if (getBool("fake.autoname")) {
      teachClassNameStrategy.autoName(teachClass)
    } else {
      val fullname = teachClassNameStrategy.genFullname(teachClass)
      teachClass.setFullname(fullname)
      teachClassNameStrategy.abbreviateName(teachClass)
    }
  }

  def notificationTemplate(): String = {
    val lessonIds = get("lessonIds")
    if (Strings.isEmpty(lessonIds)) {
      return forwardError("error.model.id.needed")
    }
    val teachTaskIdsStr = Strings.split(lessonIds, ",")
    val teachTaskAmount = teachTaskIdsStr.length
    put("taskCount", java.lang.Integer.valueOf(teachTaskAmount))
    forward()
  }

  def mergeSetting(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    if (null == lessonIds || lessonIds.length == 0) {
      return forwardError("error.teachTask.id.needed")
    }
    if (lessonIds.length <= 1) {
      return forwardError("error.teachTask.id.multiNeeded")
    }
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    operateViolationCheck(lessons) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.merge")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.merge")
      case _ => //break
    }
    if (lessons.size == 0) {
      return forwardError("error.teachTask.id.Needed")
    }
    val lesson1 = lessons.get(0)
    val courseCode1 = lesson1.getCourse.getCode
    for (i <- 1 until lessons.size if courseCode1 != lessons.get(i).getCourse.getCode) {
      return forwardError("error.teachTask.merge.courseCodesNotEquals")
    }
    put("lessons", lessons)
    forward("/com/ekingstar/eams/teach/lesson/task/web/action/lessonManagerCore/mergeSetting")
  }

  def merge(): String = {
    val reservedId = getLong("reservedId")
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    operateViolationCheck(lessons) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.merge")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.merge")
      case _ => //break
    }
    if (null == reservedId || lessonIds == null || lessonIds.length == 0) {
      return forwardError("error.teachTask.id.Needed")
    }
    lessonMergeSplitService.merge(lessonIds, reservedId)
    redirect("search", "info.merge.success", get("params"))
  }

  def breakSetting(): String = {
    val lessonId = getLong("lessonId")
    if (null == lessonId) {
      return forwardError("error.teachTask.id.needed")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    if (notInDataRealm(Collections.singletonList(lesson))) {
      return forwardError("error.dataRealm.insufficient")
    }
    operateViolationCheck(lesson) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.split")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.split")
      case _ => //break
    }
    val splitNum = getInt("break.splitNum")
    var tag = ""
    val tc = lesson.getTeachClass
    val adminclasses = courseLimitService.extractAdminclasses(tc)
    tag = if (CollectUtils.isEmpty(adminclasses) && CollectUtils.isEmpty(tc.getCourseTakes)) "00" else if (CollectUtils.isEmpty(courseLimitService.extractLonelyTakes(tc))) if (adminclasses.size == splitNum) "10" else "20" else if (CollectUtils.isEmpty(adminclasses)) "01" else if (splitNum == adminclasses.size) "11" else if (splitNum > adminclasses.size) "12" else "13"
    put("adminclasses", courseLimitService.extractAdminclasses(lesson.getTeachClass))
    put("splitTag", tag)
    put("lesson", lesson)
    forward("/com/ekingstar/eams/teach/lesson/task/web/action/lessonManagerCore/breakSetting")
  }

  def breakIt(): String = {
    val lessonId = getLong("lessonId")
    if (null == lessonId) {
      return forwardError("error.teachTask.id.needed")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    if (lesson == null) {
      return forwardError("error.teachTask.notExists")
    }
    if (notInDataRealm(Collections.singletonList(lesson))) {
      return forwardError("error.dataRealm.Insufficient")
    }
    val splitNum = getInt("break.splitNum")
    if (splitNum == null) {
      return forwardError("error.splitNum.notNum")
    }
    operateViolationCheck(lesson) match {
      case LESSON_VIOLATION => return forwardError("lesson.lesson.violation.split")
      case PERMIT_VIOLATION => return forwardError("lesson.college.violation.split")
      case _ => //break
    }
    val s_mode = AbstractTeachClassSplitter.getMode(get("break.splitMode"), courseLimitService, teachClassNameStrategy)
    if (classOf[AdminclassGroupMode] == s_mode.getClass) {
      var i = 0
      while (i <= splitNum) {
        val adminclassIds = Strings.splitToLong(get("adminclassIds" + i))
        s_mode.asInstanceOf[AdminclassGroupMode].getAdminclassGroups
          .add(adminclassIds)
        i += 1
      }
    } else if (classOf[NumberMode] == s_mode.getClass && lesson.getTeachClass.getCourseTakes.isEmpty) {
      return redirect("search", "没有学生选课的任务不允许按照单双号拆分", get("params"))
    }
    lessonMergeSplitService.split(lesson, splitNum, s_mode, Strings.splitToInt(get("splitUnitNums")))
    redirect("search", "info.split.success", get("params"))
  }

  def printAttendanceCheckList(): String = {
    val teachTaskIds = Strings.splitToLong(get("lessonIds"))
    if (null == teachTaskIds || teachTaskIds.length == 0) {
      return forwardError("error.model.id.needed")
    }
    val lessons = entityDao.get(classOf[Lesson], teachTaskIds)
    val taskStdCollisionMap = CollectUtils.newHashMap()
    val arrangeInfos = CollectUtils.newHashMap()
    val courseTakes = CollectUtils.newHashMap()
    var iter = lessons.iterator()
    while (iter.hasNext) {
      val task = iter.next().asInstanceOf[Lesson]
      taskStdCollisionMap.put(task.id.toString, CollectUtils.newArrayList())
      arrangeInfos.put(task.id.toString, CourseActivityDigestor.getInstance.digest(getTextResource, 
        task))
      courseTakes.put(task.id.toString, CollectUtils.newArrayList(task.getTeachClass.getCourseTakes))
      val params = new HashMap[String, Any]()
      params.put("taskId", task.id)
      params.put("semester", task.getSemester)
      val collisionQuery = new StringBuilder()
      collisionQuery.append("select stdTake.std.id from")
        .append("\n org.openurp.edu.teach.lesson.CourseTake stdTake join stdTake.lesson.courseSchedule.activities activity2")
        .append("\n where stdTake.std.id in ( select std.id from org.openurp.edu.teach.lesson.CourseTake take where take.lesson.id = :taskId )")
        .append("\n and stdTake.lesson.id <> :taskId")
        .append("\n and stdTake.lesson.semester = :semester")
        .append("\n and exists (")
        .append("\n 	select activity.id from org.openurp.edu.teach.lesson.Lesson task join task.courseSchedule.activities activity")
        .append("\n 	where task.id=:taskId")
        .append("\n 	and bitand(activity.time.state, activity2.time.state) > 0")
        .append("\n 	and activity.time.day = activity2.time.day")
        .append("\n 	and activity.time.start <= activity2.time.end")
        .append("\n 	and activity2.time.start <= activity.time.end")
        .append("\n )")
      taskStdCollisionMap.get(task.id.toString).addAll(entityDao.search(collisionQuery.toString, params))
    }
    put("lessons", lessons)
    put("arrangeInfos", arrangeInfos)
    put("courseTakes", courseTakes)
    put("taskStdCollisionMap", taskStdCollisionMap)
    forward("/com/ekingstar/eams/teach/lesson/task/web/action/lessonManagerCore/printAttendanceCheckList")
  }

  def searchStatistics(): String = {
    val semesterId = getInt("semester.id")
    val startWeek = getInt("startWeek")
    val endWeek = getInt("endWeek")
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.select("teacher.code,teacher.name,sum(lesson.course.credits)")
    query.join("lesson.teachers", "teacher")
    query.where("lesson.semester.id = :semesterId", semesterId)
    if (null != startWeek) {
      query.where("lesson.courseSchedule.startWeek =:startWeek", startWeek)
    }
    if (null != endWeek) {
      query.where("lesson.courseSchedule.endWeek =:endWeek", endWeek)
    }
    query.groupBy("teacher.code")
    query.groupBy("teacher.name")
    query.limit(getPageLimit)
    val lessons = entityDao.search(query)
    put("lessons", lessons)
    forward("creditStatistics")
  }

  def normalizeTeachClass(): String = {
    val projectId = getInt("project.id")
    val semesterId = getInt("semester.id")
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.project.id = :projectId", projectId)
      .where("lesson.semester.id = :semesterId", semesterId)
    val lessons = entityDao.search(query)
    for (lesson <- lessons) {
      teachClassNameStrategy.autoName(lesson.getTeachClass)
      entityDao.saveOrUpdate(lesson)
    }
    put("lessonCount", lessons.size)
    forward()
  }

  def normalizeTeachClassAdminclass(): String = {
    val projectId = getInt("project.id")
    val semesterId = getInt("semester.id")
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.project.id = :projectId", projectId)
      .where("lesson.semester.id = :semesterId", semesterId)
    val lessons = entityDao.search(query)
    for (lesson <- lessons) {
      val courseLimitBuilder = courseLimitService.builder(lesson.getTeachClass)
      val res = courseLimitService.xtractAdminclassLimit(lesson.getTeachClass)
      if (CollectUtils.isEmpty(res.getRight)) {
        //continue
      }
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.GRADE.getMetaId))
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.STDTYPE.getMetaId))
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.GENDER.getMetaId))
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.DEPARTMENT.getMetaId))
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.MAJOR.getMetaId))
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.DIRECTION.getMetaId))
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.ADMINCLASS.getMetaId))
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.NORMALCLASS.getMetaId))
      courseLimitBuilder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.EDUCATION.getMetaId))
      if (res.getLeft == Operator.IN) {
        courseLimitBuilder.in(res.getRight.toArray(Array()))
      } else if (res.getLeft == Operator.NOT_IN) {
        courseLimitBuilder.notIn(res.getRight.toArray(Array()))
      } else {
      }
      val emptyGroups = new ArrayList[CourseLimitGroup]()
      for (group <- lesson.getTeachClass.getLimitGroups if CollectUtils.isEmpty(group.getItems)) {
        emptyGroups.add(group)
      }
      lesson.getTeachClass.getLimitGroups.removeAll(emptyGroups)
      lessonDao.saveOrUpdate(lesson)
    }
    put("lessonCount", lessons.size)
    "normalizeTeachClass"
  }

  def normalizeNo(): String = {
    val projectId = getInt("project.id")
    val semesterId = getInt("semester.id")
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.project.id = :projectId", projectId)
      .where("lesson.semester.id = :semesterId", semesterId)
    query.orderBy("lesson.course.code")
    val lessons = entityDao.search(query)
    for (lesson <- lessons) {
      lesson.setNo(null)
    }
    entityDao.saveOrUpdate(lessons)
    lessonSeqNoGenerator.genLessonSeqNos(lessons)
    entityDao.saveOrUpdate(lessons)
    put("lessonCount", lessons.size)
    forward()
  }

  protected def isLessonExamArrange(): Boolean = {
    val obj = getSystemConfigBy("IsLessonExamArrange")
    if (obj != null && (obj == "1" || obj == "true")) true else false
  }

  protected def getSystemConfigBy(name: String): AnyRef = {
    val query = OqlBuilder.from(classOf[PropertyConfigItemBean], "item")
    query.where("item.name =:name", name)
    val list = entityDao.search(query)
    if (list.size > 0) list.get(0).getValue.asInstanceOf[AnyRef] else null
  }

  private var lessonSeqNoGenerator: LessonSeqNoGenerator = _

  def setLessonMergeSplitService(lessonMergeSplitService: LessonMergeSplitService) {
    this.lessonMergeSplitService = lessonMergeSplitService
  }

  def setLessonDao(lessonDao: LessonDao) {
    this.lessonDao = lessonDao
  }

  def setAdminclassService(adminClassService: AdminclassService) {
    this.adminClassService = adminClassService
  }

  def setBaseInfoSearchHelper(baseInfoSearchHelper: BaseInfoSearchHelper) {
    this.baseInfoSearchHelper = baseInfoSearchHelper
  }

  def setAdminClassService(adminClassService: AdminclassService) {
    this.adminClassService = adminClassService
  }

  def setLessonLogHelper(lessonLogHelper: LessonLogHelper) {
    this.lessonLogHelper = lessonLogHelper
  }

  def setLessonSeqNoGenerator(lessonSeqNoGenerator: LessonSeqNoGenerator) {
    this.lessonSeqNoGenerator = lessonSeqNoGenerator
  }

  def setTimeSettingService(timeSettingService: TimeSettingService) {
    this.timeSettingService = timeSettingService
  }

  def setLessonCollegeSwitchService(lessonCollegeSwitchService: LessonCollegeSwitchService) {
    this.lessonCollegeSwitchService = lessonCollegeSwitchService
  }

  def setLessonExamArrangeHelper(lessonExamArrangeHelper: LessonExamArrangeHelper) {
    this.lessonExamArrangeHelper = lessonExamArrangeHelper
  }

  def setCourseLimitMetaEnumProvider(courseLimitMetaEnumProvider: CourseLimitMetaEnumProvider) {
    this.courseLimitMetaEnumProvider = courseLimitMetaEnumProvider
  }

  def setCourseLimitItemContentProviderFactory(courseLimitItemContentProviderFactory: CourseLimitItemContentProviderFactory) {
    this.courseLimitItemContentProviderFactory = courseLimitItemContentProviderFactory
  }

  def setTeachClassNameStrategy(teachClassNameStrategy: TeachClassNameStrategy) {
    this.teachClassNameStrategy = teachClassNameStrategy
  }
}
