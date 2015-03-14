package org.openurp.edu.eams.teach.lesson.task.service.genstrategy.impl

import java.sql.Date
import java.text.MessageFormat
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.code.school.ClassroomType
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseType
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
import org.openurp.edu.eams.teach.lesson.task.biz.LessonGenPreview
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenObserver
import org.openurp.edu.eams.teach.lesson.task.service.genstrategy.AbstractLessonGenStrategy
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseGroupBean
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.teach.util.AdminclassQueryBuilder

import scala.collection.JavaConversions._

class ClassicLessonGenStrategy extends AbstractLessonGenStrategy {

  private var lessonDao: LessonDao = _

  private var lessonLogHelper: LessonLogHelper = _

  private var semesterService: SemesterService = _

  private var lessonPlanRelationService: LessonPlanRelationService = _

  private var courseLimitService: CourseLimitService = _

  protected override def iDo(source: String): Boolean = "MAJOR_PROGRAM" == source.toUpperCase()

  protected override def gen(context: Map[String, Any], observer: TaskGenObserver) {
    val planIds = context.get("planIds").asInstanceOf[Array[Long]]
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    val planCount = plans.size
    if (null != observer) {
      observer.notifyStart(observer.messageOf("info.taskGenInit.start") + "(" + planCount + 
        ")", planCount, null)
    }
    for (plan <- plans) {
      genLessons(plan, observer, context)
    }
    if (null != observer) {
      observer.notifyGenResult(planCount)
      observer.notifyFinish()
    }
  }

  protected override def preview(context: Map[String, Any]): AnyRef = {
    val planIds = context.get("planIds").asInstanceOf[Array[Long]]
    val res = new ArrayList[LessonGenPreview]()
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    for (plan <- plans) {
      res.add(previewLessonGen(plan, context))
    }
    res
  }

  private def genLessons(plan: MajorPlan, observer: TaskGenObserver, params: Map[String, Any]) {
    val preview = previewLessonGen(plan, params)
    val removeGenerated = true == params.get("removeGenerated")
    val semester = params.get("semester").asInstanceOf[Semester]
    if (removeGenerated) {
      observer.outputNotifyRemove(preview.getTerm, plan, "info.plan.removeGenTask", false)
    }
    try {
      lessonDao.saveGenResult(plan, semester, preview.getLessons, removeGenerated)
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

  private def previewLessonGen(plan: MajorPlan, params: Map[String, Any]): LessonGenPreview = {
    val semester = params.get("semester").asInstanceOf[Semester]
    val omitSmallTerm = true == params.get("omitSmallTerm")
    val termCalc = new TermCalculator(semesterService, semester)
    var term = -1
    term = termCalc.getTerm(plan.getProgram.getEffectiveOn, if (plan.getProgram.getInvalidOn != null) plan.getProgram.getInvalidOn else Date.valueOf("2099-09-09"), 
      omitSmallTerm)
    if (plan.getStartTerm != null) {
      term = term + plan.getStartTerm - 1
    }
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

  private def filterPlanCourses(planCourses: List[MajorPlanCourse], plan: MajorPlan, params: Map[String, Any]): String = {
    val semester = params.get("semester").asInstanceOf[Semester]
    val adminclasses = entityDao.search(AdminclassQueryBuilder.build(plan))
    new MajorPlanCourseFilter(planCourses, params, adminclasses) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        val allowNoAdminclass = true == params.get("allowNoAdminclass")
        val adminclasses = other.asInstanceOf[List[Adminclass]]
        if (CollectUtils.isEmpty(adminclasses) && !allowNoAdminclass) {
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
    for (relation <- lessonPlanRelationService.relations(plan, semester)) {
      existCourses.add(relation.getLesson.getCourse)
    }
    new MajorPlanCourseFilter(planCourses, params, existCourses) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        val removeGenerated = true == params.get("removeGenerated")
        if (removeGenerated) {
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
        val onlyGenCourseTypes = params.get("onlyGenCourseTypes").asInstanceOf[List[CourseType]]
        if (CollectUtils.isNotEmpty(onlyGenCourseTypes) && 
          !onlyGenCourseTypes.contains(planCourse.getCourseGroup.getCourseType)) {
          return true
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        val onlyGenCourses = params.get("onlyGenCourses").asInstanceOf[List[Course]]
        if (CollectUtils.isNotEmpty(onlyGenCourses) && !onlyGenCourses.contains(planCourse.getCourse)) {
          return true
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        val dontGenCourses = params.get("dontGenCourses").asInstanceOf[List[Course]]
        if (CollectUtils.isNotEmpty(dontGenCourses) && dontGenCourses.contains(planCourse.getCourse)) {
          return true
        }
        return false
      }
    }
      .filter()
    null
  }

  private def makeLessons(plan: MajorPlan, planCourses: List[MajorPlanCourse], params: Map[String, Any]): List[Lesson] = {
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

  private def makeNewLesson(planCourse: PlanCourse, 
      plan: MajorPlan, 
      adminClass: Adminclass, 
      params: Map[String, Any]): Lesson = {
    val semester = params.get("semester").asInstanceOf[Semester]
    val startWeek = params.get("startWeek").asInstanceOf[java.lang.Integer]
    val weeks = params.get("weeks").asInstanceOf[java.lang.Integer]
    val roomType = params.get("roomType").asInstanceOf[ClassroomType]
    val lesson = LessonBean.getDefault
    lesson.setProject(plan.getProgram.major.getProject)
    lesson.setTeachDepart(planCourse.department)
    lesson.setCourse(planCourse.getCourse)
    lesson.setCourseType(planCourse.getCourseGroup.getCourseType)
    lesson.setSemester(semester)
    lesson.setExamMode(planCourse.getCourse.getExamMode)
    val courseSchedule = lesson.getCourseSchedule
    var endWeek = startWeek
    val course = planCourse.getCourse
    endWeek = if (course.getWeeks != null && course.getWeeks > 0) startWeek + course.getWeeks - 1 else if (course.getWeekHour != 0) startWeek + (course.getPeriod / course.getWeekHour).toInt - 
      1 else startWeek + weeks - 1
    courseSchedule.setWeekState(WeekStates.build(startWeek + "-" + endWeek))
    courseSchedule.setRoomType(roomType)
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
      if (planCourse.getCourseGroup.isInstanceOf[MajorPlanCourseGroupBean]) {
        if (planCourse.getCourseGroup.asInstanceOf[MajorPlanCourseGroupBean]
          .direction != 
          null) {
          builder.in(planCourse.getCourseGroup.asInstanceOf[MajorPlanCourseGroupBean]
            .direction)
        }
      }
      builder.in(plan.getProgram)
    }
    teachClassNameStrategy.autoName(teachClass)
    lesson.setCreatedAt(new Date(System.currentTimeMillis()))
    lesson.setUpdatedAt(new Date(System.currentTimeMillis()))
    lesson
  }

  def setLessonDao(lessonDao: LessonDao) {
    this.lessonDao = lessonDao
  }

  def setLessonLogHelper(lessonLogHelper: LessonLogHelper) {
    this.lessonLogHelper = lessonLogHelper
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def setLessonPlanRelationService(lessonPlanRelationService: LessonPlanRelationService) {
    this.lessonPlanRelationService = lessonPlanRelationService
  }

  def setCourseLimitService(courseLimitService: CourseLimitService) {
    this.courseLimitService = courseLimitService
  }
}

abstract class MajorPlanCourseFilter(private var planCourses: List[MajorPlanCourse], protected var params: Map[String, Any])
    {

  protected var other: AnyRef = _

  def this(planCourses: List[MajorPlanCourse], params: Map[String, Any], other: AnyRef) {
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
