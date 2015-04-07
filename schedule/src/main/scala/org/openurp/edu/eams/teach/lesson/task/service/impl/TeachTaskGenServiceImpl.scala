package org.openurp.edu.eams.teach.lesson.task.service.impl

import java.sql.Date
import java.text.MessageFormat
import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.base.Course
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.service.LessonLimitGroupBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.task.biz.LessonGenPreview
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenObserver
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenParams
import org.openurp.edu.eams.teach.lesson.task.service.TeachTaskGenService
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.teach.util.AdminclassQueryBuilder
import org.openurp.edu.teach.lesson.model.LessonBean
import org.openurp.edu.eams.weekstate



class TeachTaskGenServiceImpl extends BaseServiceImpl with TeachTaskGenService {

  var lessonDao: LessonDao = _

  var lessonLogHelper: LessonLogHelper = _

  var semesterService: SemesterService = _

  var lessonLimitService: LessonLimitService = _

  var lessonPlanRelationService: LessonPlanRelationService = _

  var teachClassNameStrategy: TeachClassNameStrategy = _

  def genLessons(planIds: Array[Integer], observer: TaskGenObserver, params: TaskGenParams) {
    if (null != observer) {
      observer.notifyStart(observer.messageOf("info.taskGenInit.start") + "(" + planIds.length + 
        ")", planIds.length, null)
    }
    val plans = entityDao.find(classOf[MajorPlan], planIds)
    for (plan <- plans) {
      genLessons(plan, observer, params)
    }
    if (null != observer) {
      observer.notifyGenResult(planIds.length)
      observer.notifyFinish()
    }
    return
  }

  def previewLessonGen(planIds: Array[Integer], params: TaskGenParams): Seq[LessonGenPreview] = {
    val res = Collections.newBuffer[LessonGenPreview]()
    val plans = entityDao.find(classOf[MajorPlan], planIds)
    for (plan <- plans) {
      res += previewLessonGen(plan, params)
    }
    res
  }

  private def previewLessonGen(plan: MajorPlan, params: TaskGenParams): LessonGenPreview = {
    val termCalc = new TermCalculator(semesterService, params.semester)
    var term = -1
    term = if (plan.program.beginOn != null) termCalc.getTerm(plan.program.beginOn, 
      plan.program.endOn, params.omitSmallTerm) else termCalc.getTerm(plan.program.beginOn, 
      Date.valueOf("2099-09-09"), params.omitSmallTerm)
    val preview = new LessonGenPreview(plan, term)
    if (term <= 0) {
      preview.error = "还没到该计划生成任务的时候"
      return preview
    }
    val planCourses = getPlanCourses(preview)
    if (Strings.isNotEmpty(preview.error)) {
      return preview
    }
    preview.error = filterPlanCourses(planCourses, plan, params)
    preview.lessons ++= makeLessons(plan, planCourses, params)
    preview
  }

  private def getPlanCourses(preview: LessonGenPreview): Seq[PlanCourse] = {
    val planCourses = PlanUtils.getPlanCourses(preview.plan, preview.term)
    if (Collections.isEmpty(planCourses)) {
      preview.error = MessageFormat.format("该计划在第{0}学期没有课程", preview.term)
    }
    planCourses
  }

  private def filterPlanCourses(planCourses: Seq[PlanCourse], plan: MajorPlan, params: TaskGenParams): String = {
    val adminclasses = entityDao.search(AdminclassQueryBuilder.build(plan))
    new MajorPlanCourseFilter(planCourses, params, adminclasses) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        val adminclasses = other.asInstanceOf[List[Adminclass]]
        if (Collections.isEmpty(adminclasses) && !params.allowNoAdminclass) {
          return true
        }
        return false
      }
    }
      .filter()
    if (Collections.isEmpty(adminclasses) && Collections.isEmpty(planCourses)) {
      return "没有行政班无法生成任务"
    }
    val existCourses = Collections.newSet[Course]
    for (relation <- lessonPlanRelationService.relations(plan, params.semester)) {
      existCourses.add(relation.getLesson.getCourse)
    }
    new MajorPlanCourseFilter(planCourses, params, existCourses) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (params.removeGenerated) {
          return false
        }
        val courses = other.asInstanceOf[Set[Course]]
        for (course <- courses if planCourse.course == course) {
          return true
        }
        return false
      }
    }
      .filter()
    if (Collections.isNotEmpty(existCourses) && Collections.isEmpty(planCourses)) {
      return "所有课程都已生成过任务"
    }
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (!params.ignoreCloseRequest) {
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (Collections.isNotEmpty(params.onlyGenCourseTypes) && 
          !params.onlyGenCourseTypes.contains(planCourse.group.courseType)) {
          return true
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (Collections.isNotEmpty(params.onlyGenCourses) && 
          !params.onlyGenCourses.contains(planCourse.course)) {
          return true
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (params.dontGenCourses.contains(planCourse.course)) {
          return true
        }
        return false
      }
    }
      .filter()
    null
  }

  private def makeLessons(plan: MajorPlan, planCourses: Seq[PlanCourse], params: TaskGenParams): Seq[Lesson] = {
    val res = Collections.newBuffer[Lesson]
    if (Collections.isEmpty(planCourses)) {
      return res
    }
    val adminclasses = entityDao.search(AdminclassQueryBuilder.build(plan))
    if (Collections.isNotEmpty(adminclasses)) {
      for (adminclass <- adminclasses) {
        val lessons = Collections.newBuffer[Lesson]()
        for (planCourse <- planCourses) {
          val lesson = makeNewLesson(planCourse, plan, adminclass, params)
          lessons += lesson
        }
        res ++= lessons
      }
    } else {
      val lessons = Collections.newBuffer[Lesson]
      for (planCourse <- planCourses) {
        val lesson = makeNewLesson(planCourse, plan, null, params)
        lessons += lesson
      }
      res ++= lessons
    }
    res
  }

  private def genLessons(plan: MajorPlan, observer: TaskGenObserver, params: TaskGenParams) {
    val preview = previewLessonGen(plan, params)
    if (params.removeGenerated ) {
      observer.outputNotifyRemove(preview.term, plan, "info.plan.removeGenTask", false)
    }
    try {
      lessonDao.saveGenResult(plan, params.semester, preview.lessons, params.removeGenerated)
      for (lesson <- preview.lessons) {
        lessonLogHelper.log(LessonLogBuilder.create(lesson, "生成任务"))
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        observer.outputNotifyRemove(0, plan, "info.plan.failure.removeGenTask", false)
        return
      }
    }
    if (null != observer) {
      observer.outputNotify(preview.term, preview.lessons.size, plan)
    }
  }

  private def makeNewLesson(planCourse: PlanCourse, 
      plan: MajorPlan, 
      adminClass: Adminclass, 
      params: TaskGenParams): Lesson = {
    val lesson = new LessonBean
    lesson.project = plan.program.major.project
    lesson.teachDepart = planCourse.department
    lesson.course = planCourse.course
    lesson.courseType = planCourse.group.courseType
    lesson.semester = params.semester
//    lesson.examMode = planCourse.getCourse.getExamMode
    val courseSchedule = lesson.schedule
    val startWeek = params.startWeek
    var endWeek = startWeek
    val course = planCourse.course
    endWeek = if (course.weeks != null && course.weeks > 0) params.startWeek + course.weeks - 1 
    else if (course.weekHour != 0) params.startWeek + (course.period / course.weekHour).toInt - 1 
    else params.startWeek + params.weeks - 1
    courseSchedule.weekState = WeekStates.build(startWeek + "-" + endWeek)
    courseSchedule.roomType = params.classroomType
    val teachClass = lesson.teachClass
    teachClass.grade = plan.program.grade
    teachClass.depart = plan.program.department
    val builder = lessonLimitService.builder(teachClass)
    if (null != adminClass) {
      teachClass.limitCount = if (adminClass.stdCount == 0) adminClass.planCount else adminClass.stdCount
//      if (adminClass.stdCount == 0) {
//        teachClass.limitCount = adminClass.planCount
//      } else {
//        teachClass.limitCount = adminClass.stdCount
//      }
      builder.in(adminClass)
    } else {
      builder.inGrades(plan.program.grade)
      builder.in(plan.program.education)
      if (plan.program.stdType != null) {
        builder.in(plan.program.stdType)
      }
      builder.in(plan.program.department)
      builder.in(plan.program.major)
      if (plan.program.direction != null) {
        builder.in(plan.program.direction)
      }
      builder.in(plan.program)
    }
    teachClassNameStrategy.autoName(teachClass)
//    lesson.setCreatedAt(new Date(System.currentTimeMillis()))
    lesson.updatedAt = new Date(System.currentTimeMillis())
    lesson
  }
}

abstract class MajorPlanCourseFilter(private var planCourses: Seq[PlanCourse], protected var params: TaskGenParams)
    {

  protected var other: AnyRef = _

  def this(planCourses: Seq[PlanCourse], params: TaskGenParams, other: AnyRef) {
    this()
    this.planCourses = planCourses
    this.params = params
    this.other = other
  }

  def filter() {
    val removeIndecies = Collections.newBuffer[Integer](20)
    for (i <- 0 until planCourses.size if shouldRemove(planCourses(i))) {
        i ::= removeIndecies
    }
    for (i <- removeIndecies) {
      planCourses --= (i.intValue())
    }
  }

  def shouldRemove(planCourse: PlanCourse): Boolean
}
