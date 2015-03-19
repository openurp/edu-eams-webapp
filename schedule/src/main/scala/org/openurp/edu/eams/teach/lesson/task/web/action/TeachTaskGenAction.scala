package org.openurp.edu.eams.teach.lesson.task.web.action

import java.sql.Date



import javax.servlet.http.HttpServletResponse
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.eams.base.code.school.RoomType
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.eams.teach.lesson.dao.LessonSeqNoGenerator
import org.openurp.edu.eams.teach.lesson.model.LessonBean
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.task.service.LessonGenService
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenObserver
import org.openurp.edu.eams.teach.major.helper.MajorPlanSearchHelper
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.program.share.SharePlan
import org.openurp.edu.eams.teach.program.share.SharePlanCourse
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class TeachTaskGenAction extends SemesterSupportAction {

  protected var majorPlanSearchHelper: MajorPlanSearchHelper = _

  protected var lessonGenService: LessonGenService = _

  protected var lessonSeqNoGenerator: LessonSeqNoGenerator = _

  protected var lessonLogHelper: LessonLogHelper = _

  protected var teachClassNameStrategy: TeachClassNameStrategy = _

  def index(): String = {
    put("stateList", CommonAuditState.values)
    setSemesterDataRealm(hasStdTypeCollege)
    forward()
  }

  def search(): String = {
    val query = majorPlanSearchHelper.buildPlanQuery()
    query.join("left outer", "plan.program.direction", "direction")
    query.where("plan.program.major.project in (:projects)", getProjects)
      .where("plan.program.department in (:departments)", getDeparts)
      .where("plan.program.stdType in (:stdTypes)", getStdTypes)
    if (CollectUtils.isNotEmpty(getEducations)) {
      query.where("plan.program.education in (:educations)", getEducations)
    }
    val semester = semesterService.getSemester(getInt("semester.id"))
    val generated = getBool("generated")
    if (generated) {
      query.where("exists (select relation.id from org.openurp.edu.teach.lesson.LessonPlanRelation relation " + 
        "where relation.plan=plan and relation.lesson.semester = :semester)", semester)
    } else {
      query.where("not exists (select relation.id from org.openurp.edu.teach.lesson.LessonPlanRelation relation " + 
        "where relation.plan=plan and relation.lesson.semester = :semester)", semester)
    }
    majorPlanSearchHelper.addSemesterActiveCondition(query, semester)
    val plans = entityDao.search(query)
    put("plans", plans)
    put("semester", semester)
    forward()
  }

  def info(): String = {
    val planIds = Strings.splitToLong(get("planIds"))
    val semester = entityDao.get(classOf[Semester], getInt("semester.id"))
    val query = OqlBuilder.from(classOf[LessonPlanRelation], "relation")
    query.where("relation.plan.id in (:planIds)", planIds)
      .where("relation.lesson.semester = :semester", semester)
      .orderBy("relation.plan.id")
      .orderBy("relation.lesson.no")
    put("relations", entityDao.search(query))
    forward()
  }

  def genSetting(): String = {
    val semester = semesterService.getSemester(getInt("semester.id"))
    put("classroomTypeList", baseCodeService.getCodes(classOf[RoomType]))
    put("startWeek", 1)
    put("weeks", semester.getWeeks)
    put("semester", semester)
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    forward()
  }

  def genPreview(): String = {
    val params = getGenContext
    put("genPreviews", lessonGenService.preview("MAJOR_PROGRAM", params))
    put("params", params)
    forward()
  }

  def gen(): String = {
    val context = getGenContext
    val response = getResponse
    response.setContentType("text/html; charset=utf-8")
    val observer = getOutputProcessObserver(classOf[TaskGenObserver]).asInstanceOf[TaskGenObserver]
    lessonGenService.gen("MAJOR_PROGRAM", context, observer)
    response.getWriter.flush()
    response.getWriter.close()
    null
  }

  protected def getGenContext(): Map[String, Any] = {
    val params = CollectUtils.newHashMap()
    params.put("planIds", Strings.splitToLong(get("planIds")))
    params.put("semester", entityDao.get(classOf[Semester], getInt("params.semester.id")))
    params.put("startWeek", getInt("params.startWeek"))
    params.put("weeks", getInt("params.weeks"))
    params.put("removeGenerated", getBool("params.removeGenerated"))
    params.put("allowNoAdminclass", getBool("params.allowNoAdminclass"))
    params.put("omitSmallTerm", getBool("params.omitSmallTerm"))
    params.put("roomType", entityDao.get(classOf[RoomType], getInt("params.roomType.id")))
    val onlyGenCourseIds = Strings.splitToLong(get("fake.onlyGenCourseIds"))
    val dontGenCourseIds = Strings.splitToLong(get("fake.dontGenCourseIds"))
    val onlyGenCourseTypeIds = Strings.splitToInt(get("fake.onlyGenCourseTypeIds"))
    if (onlyGenCourseIds.length > 0) {
      params.put("onlyGenCourses", entityDao.get(classOf[Course], onlyGenCourseIds))
    }
    if (dontGenCourseIds.length > 0) {
      params.put("dontGenCourses", entityDao.get(classOf[Course], dontGenCourseIds))
    }
    if (onlyGenCourseTypeIds.length > 0) {
      params.put("onlyGenCourseTypes", entityDao.get(classOf[CourseType], onlyGenCourseTypeIds))
    }
    params
  }

  def genShareLessons(): String = {
    val semesterId = getInt("semester.id")
    val semester = entityDao.get(classOf[Semester], semesterId)
    val sharePlanCourses = getModels(classOf[SharePlanCourse], ids("targetCourse", classOf[Long]))
    val lessons = CollectUtils.newArrayList()
    for (sharePlanCourse <- sharePlanCourses) {
      val course = sharePlanCourse.getCourse
      val lesson = LessonBean.getDefault
      lesson.setProject(getProject)
      lesson.setTeachDepart(course.department)
      lesson.setCourse(course)
      lesson.setCourseType(sharePlanCourse.getCourseGroup.getCourseType)
      lesson.setSemester(semester)
      lesson.setExamMode(course.getExamMode)
      val startWeek = 1
      var endWeek = 1
      endWeek = if (course.getWeeks != null && course.getWeeks > 0) course.getWeeks else if (course.getWeekHour != 0) (course.getPeriod / course.getWeekHour).toInt else semester.getWeeks
      lesson.getCourseSchedule.setWeekState(WeekStates.build(startWeek + "-" + endWeek))
      lesson.setCreatedAt(new Date(System.currentTimeMillis()))
      lesson.setUpdatedAt(new Date(System.currentTimeMillis()))
      teachClassNameStrategy.autoName(lesson.getTeachClass)
      lessons.add(lesson)
    }
    lessonSeqNoGenerator.genLessonSeqNos(lessons)
    for (lesson <- lessons) {
      lesson.setAuditStatus(CommonAuditState.UNSUBMITTED)
    }
    try {
      entityDao.saveOrUpdate(lessons)
    } catch {
      case e: Exception => return redirect("plannedShareCourseSelect", "生成失败")
    }
    for (lesson <- lessons) {
      lessonLogHelper.log(LessonLogBuilder.create(lesson, "生成任务"))
    }
    redirect("plannedShareCourseSelect", "成功生成" + lessons.size + "条")
  }

  def plannedShareCourseSelect(): String = {
    val semester = putSemester(null)
    if (semester == null) {
      return forwardError("未指定学期")
    }
    val query = OqlBuilder.from(classOf[SharePlan], "sharePlan").where("sharePlan.effectiveOn <= :now and (sharePlan.invalidOn is null or sharePlan.invalidOn >= :now)", 
      new java.util.Date())
    val sharePlans = entityDao.search(query)
    if (sharePlans.isEmpty) {
      return forwardError("没有生效的公共计划")
    }
    put("sharePlans", sharePlans)
    val sharePlanId = getLong("sharePlan.id")
    var sharePlan: SharePlan = null
    sharePlan = if (sharePlanId == null) sharePlans.get(0) else entityDao.get(classOf[SharePlan], sharePlanId)
    put("sharePlan", sharePlan)
    val termCalc = new TermCalculator(semesterService, semester)
    var term = -1
    term = if (sharePlan.getInvalidOn != null) termCalc.getTerm(sharePlan.getEffectiveOn, sharePlan.getInvalidOn, 
      true) else termCalc.getTerm(sharePlan.getEffectiveOn, Date.valueOf("2099-09-09"), true)
    val builder = OqlBuilder.from(classOf[SharePlanCourse], "sharePlanCourse")
    populateConditions(builder)
    builder.where("sharePlanCourse.courseGroup.plan = :plan", sharePlan)
    val orderBy = get(Order.ORDER_STR)
    if (Strings.isEmpty(orderBy)) {
      builder.orderBy("sharePlanCourse.courseGroup.courseType.id")
    } else {
      builder.orderBy(orderBy)
    }
    val sharePlanCourses = entityDao.search(builder)
    val spCourses = CollectUtils.newArrayList()
    val courseTypes = CollectUtils.newHashSet()
    val departments = CollectUtils.newHashSet()
    for (sharePlanCourse <- sharePlanCourses if TermCalculator.inTerm(sharePlanCourse.getTerms, term)) {
      spCourses.add(sharePlanCourse)
      courseTypes.add(sharePlanCourse.getCourseGroup.getCourseType)
      val department = sharePlanCourse.getCourse.department
      if (null != department) {
        departments.add(department)
      }
    }
    put("spCourses", spCourses)
    put("courseTypes", courseTypes)
    put("departments", departments)
    put("semester", semester)
    forward()
  }

  def courses(): String = {
    val codeOrName = get("term")
    val query = OqlBuilder.from(classOf[Course], "course")
    if (Strings.isNotEmpty(codeOrName)) {
      query.where("(course.name like :name or course.code like :code)", '%' + codeOrName + '%', '%' + codeOrName + '%')
    }
    query.where("course.enabled = true").where("course.department in (:departs)", getDeparts)
      .where("course.project= :project", getProject)
      .orderBy("course.code")
      .limit(getPageLimit)
    put("courses", entityDao.search(query))
    forward("coursesJSON")
  }

  def setMajorPlanSearchHelper(majorPlanSearchHelper: MajorPlanSearchHelper) {
    this.majorPlanSearchHelper = majorPlanSearchHelper
  }

  def setLessonGenService(lessonGenService: LessonGenService) {
    this.lessonGenService = lessonGenService
  }

  def setLessonSeqNoGenerator(lessonSeqNoGenerator: LessonSeqNoGenerator) {
    this.lessonSeqNoGenerator = lessonSeqNoGenerator
  }

  def setLessonLogHelper(lessonLogHelper: LessonLogHelper) {
    this.lessonLogHelper = lessonLogHelper
  }

  def setTeachClassNameStrategy(teachClassNameStrategy: TeachClassNameStrategy) {
    this.teachClassNameStrategy = teachClassNameStrategy
  }
}
