package org.openurp.edu.eams.teach.lesson.task.service.impl

import java.sql.Date
import java.text.MessageFormat
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.lesson.CourseSchedule
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.model.LessonBean
import org.openurp.edu.eams.teach.lesson.service.CourseLimitGroupBuilder
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.task.biz.LessonGenPreview
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenObserver
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenParams
import org.openurp.edu.eams.teach.lesson.task.service.TeachTaskGenService
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.teach.util.AdminclassQueryBuilder

import scala.collection.JavaConversions._

class TeachTaskGenServiceImpl extends BaseServiceImpl with TeachTaskGenService {

  private var lessonDao: LessonDao = _

  private var lessonLogHelper: LessonLogHelper = _

  private var semesterService: SemesterService = _

  private var courseLimitService: CourseLimitService = _

  private var lessonPlanRelationService: LessonPlanRelationService = _

  private var teachClassNameStrategy: TeachClassNameStrategy = _

  def genLessons(planIds: Array[Long], observer: TaskGenObserver, params: TaskGenParams) {
    if (null != observer) {
      observer.notifyStart(observer.messageOf("info.taskGenInit.start") + "(" + planIds.length + 
        ")", planIds.length, null)
    }
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    for (plan <- plans) {
      genLessons(plan, observer, params)
    }
    if (null != observer) {
      observer.notifyGenResult(planIds.length)
      observer.notifyFinish()
    }
    return
  }

  def previewLessonGen(planIds: Array[Long], params: TaskGenParams): List[LessonGenPreview] = {
    val res = new ArrayList[LessonGenPreview]()
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    for (plan <- plans) {
      res.add(previewLessonGen(plan, params))
    }
    res
  }

  private def previewLessonGen(plan: MajorPlan, params: TaskGenParams): LessonGenPreview = {
    val termCalc = new TermCalculator(semesterService, params.getSemester)
    var term = -1
    term = if (plan.getProgram.getInvalidOn != null) termCalc.getTerm(plan.getProgram.getEffectiveOn, 
      plan.getProgram.getInvalidOn, params.isOmitSmallTerm) else termCalc.getTerm(plan.getProgram.getEffectiveOn, 
      Date.valueOf("2099-09-09"), params.isOmitSmallTerm)
    val preview = new LessonGenPreview(plan, term)
    if (term <= 0) {
      preview.setError("还没到该计划生成任务的时候")
      return preview
    }
    val planCourses = getPlanCourses(preview)
    if (Strings.isNotEmpty(preview.getError)) {
      return preview
    }
    preview.setError(filterPlanCourses(planCourses, plan, params))
    preview.getLessons.addAll(makeLessons(plan, planCourses, params))
    preview
  }

  private def getPlanCourses(preview: LessonGenPreview): List[MajorPlanCourse] = {
    val planCourses = PlanUtils.getPlanCourses(preview.getPlan, preview.getTerm)
    if (CollectUtils.isEmpty(planCourses)) {
      preview.setError(MessageFormat.format("该计划在第{0}学期没有课程", preview.getTerm))
    }
    planCourses
  }

  private def filterPlanCourses(planCourses: List[MajorPlanCourse], plan: MajorPlan, params: TaskGenParams): String = {
    val adminclasses = entityDao.search(AdminclassQueryBuilder.build(plan))
    new MajorPlanCourseFilter(planCourses, params, adminclasses) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        val adminclasses = other.asInstanceOf[List[Adminclass]]
        if (CollectUtils.isEmpty(adminclasses) && !params.isAllowNoAdminclass) {
          return true
        }
        return false
      }
    }
      .filter()
    if (CollectUtils.isEmpty(adminclasses) && CollectUtils.isEmpty(planCourses)) {
      return "没有行政班无法生成任务"
    }
    val existCourses = new HashSet[Course]()
    for (relation <- lessonPlanRelationService.relations(plan, params.getSemester)) {
      existCourses.add(relation.getLesson.getCourse)
    }
    new MajorPlanCourseFilter(planCourses, params, existCourses) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (params.isRemoveGenerated) {
          return false
        }
        val courses = other.asInstanceOf[Set[Course]]
        for (course <- courses if planCourse.getCourse == course) {
          return true
        }
        return false
      }
    }
      .filter()
    if (CollectUtils.isNotEmpty(existCourses) && CollectUtils.isEmpty(planCourses)) {
      return "所有课程都已生成过任务"
    }
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (!params.isIgnoreCloseRequest) {
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (CollectUtils.isNotEmpty(params.getOnlyGenCourseTypes) && 
          !params.getOnlyGenCourseTypes.contains(planCourse.getCourseGroup.getCourseType)) {
          return true
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (CollectUtils.isNotEmpty(params.getOnlyGenCourses) && 
          !params.getOnlyGenCourses.contains(planCourse.getCourse)) {
          return true
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        if (params.getDontGenCourses.contains(planCourse.getCourse)) {
          return true
        }
        return false
      }
    }
      .filter()
    null
  }

  private def makeLessons(plan: MajorPlan, planCourses: List[MajorPlanCourse], params: TaskGenParams): List[Lesson] = {
    val res = new ArrayList[Lesson]()
    if (CollectUtils.isEmpty(planCourses)) {
      return res
    }
    val adminclasses = entityDao.search(AdminclassQueryBuilder.build(plan))
    if (CollectUtils.isNotEmpty(adminclasses)) {
      for (adminclass <- adminclasses) {
        val lessons = new ArrayList[Lesson]()
        for (planCourse <- planCourses) {
          val lesson = makeNewLesson(planCourse, plan, adminclass, params)
          lessons.add(lesson)
        }
        res.addAll(lessons)
      }
    } else {
      val lessons = new ArrayList[Lesson]()
      for (planCourse <- planCourses) {
        val lesson = makeNewLesson(planCourse, plan, null, params)
        lessons.add(lesson)
      }
      res.addAll(lessons)
    }
    res
  }

  private def genLessons(plan: MajorPlan, observer: TaskGenObserver, params: TaskGenParams) {
    val preview = previewLessonGen(plan, params)
    if (params.isRemoveGenerated) {
      observer.outputNotifyRemove(preview.getTerm, plan, "info.plan.removeGenTask", false)
    }
    try {
      lessonDao.saveGenResult(plan, params.getSemester, preview.getLessons, params.isRemoveGenerated)
      for (lesson <- preview.getLessons) {
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
      observer.outputNotify(preview.getTerm, preview.getLessons.size, plan)
    }
  }

  private def makeNewLesson(planCourse: PlanCourse, 
      plan: MajorPlan, 
      adminClass: Adminclass, 
      params: TaskGenParams): Lesson = {
    val lesson = LessonBean.getDefault
    lesson.setProject(plan.getProgram.major.getProject)
    lesson.setTeachDepart(planCourse.department)
    lesson.setCourse(planCourse.getCourse)
    lesson.setCourseType(planCourse.getCourseGroup.getCourseType)
    lesson.setSemester(params.getSemester)
    lesson.setExamMode(planCourse.getCourse.getExamMode)
    val courseSchedule = lesson.getCourseSchedule
    val startWeek = params.getStartWeek
    var endWeek = startWeek
    val course = planCourse.getCourse
    endWeek = if (course.getWeeks != null && course.getWeeks > 0) params.getStartWeek + course.getWeeks - 1 else if (course.getWeekHour != 0) params.getStartWeek + (course.getPeriod / course.getWeekHour).toInt - 
      1 else params.getStartWeek + params.getWeeks - 1
    courseSchedule.setWeekState(WeekStates.build(startWeek + "-" + endWeek))
    courseSchedule.setRoomType(params.getRoomType)
    val teachClass = lesson.getTeachClass
    teachClass.setGrade(plan.getProgram.grade)
    teachClass.setDepart(plan.getProgram.department)
    val builder = courseLimitService.builder(teachClass)
    if (null != adminClass) {
      if (adminClass.getStdCount == 0) {
        teachClass.setLimitCount(adminClass.getPlanCount)
      } else {
        teachClass.setLimitCount(adminClass.getStdCount)
      }
      builder.in(adminClass)
    } else {
      builder.inGrades(plan.getProgram.grade)
      builder.in(plan.getProgram.education)
      if (plan.getProgram.stdType != null) {
        builder.in(plan.getProgram.stdType)
      }
      builder.in(plan.getProgram.department)
      builder.in(plan.getProgram.major)
      if (plan.getProgram.direction != null) {
        builder.in(plan.getProgram.direction)
      }
      builder.in(plan.getProgram)
    }
    teachClassNameStrategy.autoName(teachClass)
    lesson.setCreatedAt(new Date(System.currentTimeMillis()))
    lesson.setUpdatedAt(new Date(System.currentTimeMillis()))
    lesson
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def setLessonDao(lessonDao: LessonDao) {
    this.lessonDao = lessonDao
  }

  def setCourseLimitService(courseLimitService: CourseLimitService) {
    this.courseLimitService = courseLimitService
  }

  def setLessonPlanRelationService(lessonPlanRelationService: LessonPlanRelationService) {
    this.lessonPlanRelationService = lessonPlanRelationService
  }

  def setLessonLogHelper(lessonLogHelper: LessonLogHelper) {
    this.lessonLogHelper = lessonLogHelper
  }

  def setTeachClassNameStrategy(teachClassNameStrategy: TeachClassNameStrategy) {
    this.teachClassNameStrategy = teachClassNameStrategy
  }
}

abstract class MajorPlanCourseFilter(private var planCourses: List[MajorPlanCourse], protected var params: TaskGenParams)
    {

  protected var other: AnyRef = _

  def this(planCourses: List[MajorPlanCourse], params: TaskGenParams, other: AnyRef) {
    super()
    this.planCourses = planCourses
    this.params = params
    this.other = other
  }

  def filter() {
    val removeIndecies = new ArrayList[Integer](20)
    for (i <- 0 until planCourses.size if shouldRemove(planCourses.get(i))) {
      removeIndecies.add(0, i)
    }
    for (i <- removeIndecies) {
      planCourses.remove(i.intValue())
    }
  }

  def shouldRemove(planCourse: MajorPlanCourse): Boolean
}
