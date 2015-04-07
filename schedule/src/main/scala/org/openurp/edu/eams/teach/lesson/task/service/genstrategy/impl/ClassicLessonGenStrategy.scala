package org.openurp.edu.eams.teach.lesson.task.service.genstrategy.impl

import java.sql.Date
import java.text.MessageFormat
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.task.biz.LessonGenPreview
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.eams.teach.lesson.task.service.TaskGenObserver
import org.openurp.edu.eams.teach.lesson.task.service.genstrategy.AbstractLessonGenStrategy
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.lesson.model.LessonBean
import org.openurp.edu.teach.plan.model.MajorCourseGroupBean
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.base.code.RoomType
import scala.collection.mutable.HashSet
import java.util.ArrayList
import org.openurp.edu.teach.lesson.model.LessonBean
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.teach.util.AdminclassQueryBuilder
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.weekstate.WeekStates
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder



class ClassicLessonGenStrategy extends AbstractLessonGenStrategy {

  private var lessonDao: LessonDao = _

  private var lessonLogHelper: LessonLogHelper = _

  private var semesterService: SemesterService = _

  private var lessonPlanRelationService: LessonPlanRelationService = _

  private var lessonLimitService: LessonLimitService = _

  protected override def iDo(source: String): Boolean = "MAJOR_PROGRAM" == source.toUpperCase()

  protected override def gen(context: Map[String, Any], observer: TaskGenObserver) {
    val planIds = context.get("planIds").asInstanceOf[Integer]
    val plans = entityDao.find(classOf[MajorPlan], planIds)
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
    val planIds = context.get("planIds").asInstanceOf[Integer]
    val res = new ArrayList[LessonGenPreview]()
    val plans = entityDao.find(classOf[MajorPlan], planIds)
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
      observer.outputNotifyRemove(preview.term, plan, "info.plan.removeGenTask", false)
    }
    try {
      lessonDao.saveGenResult(plan, semester, preview.lessons, removeGenerated)
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

  private def previewLessonGen(plan: MajorPlan, params: Map[String, Any]): LessonGenPreview = {
    val semester = params.get("semester").asInstanceOf[Semester]
    val omitSmallTerm = true == params.get("omitSmallTerm")
    val termCalc = new TermCalculator(semesterService, semester)
    var term = -1
    term = termCalc.getTerm(plan.program.beginOn, if (plan.program.endOn != null) plan.program.endOn else Date.valueOf("2099-09-09"), 
      omitSmallTerm)
    if (plan.startTerm != null) {
      term = term + plan.startTerm - 1
    }
    val preview = new LessonGenPreview(plan, term)
    if (term <= 0) {
      preview.error ="还没到该计划生成任务的时候"
      return preview
    }
    val planCourses = getPlanCourses(preview)
    if (Strings.isNotEmpty(preview.error)) {
      return preview
    }
    preview.error = filterPlanCourses(planCourses, plan, params)
    preview.lessons ++=(makeLessons(plan, planCourses, params))
    preview
  }

  private def getPlanCourses(preview: LessonGenPreview) : Seq[PlanCourse] = {
    val planCourses = PlanUtils.getPlanCourses(preview.plan, preview.term)
    if (Collections.isEmpty(planCourses)) {
      preview.error = (MessageFormat.format("该计划在第{0}学期没有课程", preview.term.asInstanceOf[Object]))
    }
    planCourses
  }

  private def filterPlanCourses(planCourses: Seq[PlanCourse], plan: MajorPlan, params: Map[String, Any]): String = {
    val semester = params.get("semester").asInstanceOf[Semester]
    val adminclasses = entityDao.search(AdminclassQueryBuilder.build(plan))
    new MajorPlanCourseFilter(planCourses, params, adminclasses) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        val allowNoAdminclass = true == params.get("allowNoAdminclass")
        val adminclasses = other.asInstanceOf[List[Adminclass]]
        if (Collections.isEmpty(adminclasses) && !allowNoAdminclass) {
          return true
        }
        return false
      }
    }
      .filter()
    if (Collections.isEmpty(adminclasses) && Collections.isEmpty(planCourses)) {
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
        val onlyGenCourseTypes = params.get("onlyGenCourseTypes").asInstanceOf[List[CourseType]]
        if (Collections.isNotEmpty(onlyGenCourseTypes) && 
          !onlyGenCourseTypes.contains(planCourse.group.courseType)) {
          return true
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        val onlyGenCourses = params.get("onlyGenCourses").asInstanceOf[List[Course]]
        if (Collections.isNotEmpty(onlyGenCourses) && !onlyGenCourses.contains(planCourse.course)) {
          return true
        }
        return false
      }
    }
      .filter()
    new MajorPlanCourseFilter(planCourses, params) {

      override def shouldRemove(planCourse: MajorPlanCourse): Boolean = {
        val dontGenCourses = params.get("dontGenCourses").asInstanceOf[List[Course]]
        if (Collections.isNotEmpty(dontGenCourses) && dontGenCourses.contains(planCourse.course)) {
          return true
        }
        return false
      }
    }
      .filter()
    null
  }

  private def makeLessons(plan: MajorPlan, planCourses: Seq[PlanCourse], params: Map[String, Any]): collection.mutable.Buffer[Lesson] = {
    val res = Collections.newBuffer[Lesson]
    if (Collections.isEmpty(planCourses)) {
      res
    }
    val adminclasses = entityDao.search(AdminclassQueryBuilder.build(plan))
    if (Collections.isNotEmpty(adminclasses)) {
      for (adminclass <- adminclasses) {
        val lessons = Collections.newBuffer[Lesson]
        for (planCourse <- planCourses) {
          val lesson = makeNewLesson(planCourse, plan, adminclass, params)
          lessons+=lesson
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

  private def makeNewLesson(planCourse: PlanCourse, 
      plan: MajorPlan, 
      adminClass: Adminclass, 
      params: Map[String, Any]): Lesson = {
    val semester = params.get("semester").asInstanceOf[Semester]
    val startWeek = params.get("startWeek").asInstanceOf[java.lang.Integer]
    val weeks = params.get("weeks").asInstanceOf[java.lang.Integer]
    val roomType = params.get("roomType").asInstanceOf[RoomType]
    val lesson = new LessonBean
    lesson.project = plan.program.major.project
    lesson.teachDepart = planCourse.department
    lesson.course = planCourse.course
    lesson.courseType = planCourse.group.courseType
    lesson.semester = semester
//    lesson.examMode = planCourse.course.examMode
    val courseSchedule = lesson.schedule
    var endWeek = startWeek
    val course = planCourse.course
    endWeek = if (course.weeks != null && course.weeks > 0) startWeek + course.weeks - 1 else if (course.weekHour != 0) startWeek + (course.period / course.weekHour).toInt - 
      1 else startWeek + weeks - 1
    courseSchedule.weekState = WeekStates.build(startWeek + "-" + endWeek)
    courseSchedule.roomType = roomType
    val teachClass = lesson.teachClass
    teachClass.grade = plan.program.grade
    teachClass.depart = plan.program.department
    val builder = lessonLimitService.builder(teachClass)
    if (null != adminClass) {
      if (adminClass.stdCount == 0) {
        teachClass.limitCount = adminClass.planCount
      } else {
        teachClass.limitCount = adminClass.stdCount
      }
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
      if (planCourse.group.isInstanceOf[MajorCourseGroupBean]) {
        if (planCourse.group.asInstanceOf[MajorCourseGroupBean]
          .direction != 
          null) {
          builder.in(planCourse.group.asInstanceOf[MajorCourseGroupBean]
            .direction)
        }
      }
      builder.in(plan.program)
    }
    teachClassNameStrategy.autoName(teachClass)
    lesson.updatedAt = new Date(System.currentTimeMillis())
    lesson
  }

}

abstract class MajorPlanCourseFilter(private var planCourses: Seq[PlanCourse], protected var params: Map[String, Any]){

  protected var other: AnyRef = _

  def this(planCourses: Seq[MajorPlanCourse], params: Map[String, Any], other: AnyRef) {
    this()
    this.planCourses = planCourses
    this.params = params
    this.other = other
  }

  def filter() {
    val removeIndecies = Collections.newBuffer[Integer](20)
    for (i <- 0 until planCourses.size if shouldRemove(planCourses(i))) {
      removeIndecies.add (0, i)
    }
    for (i <- removeIndecies) {
      planCourses.remove(i.intValue())
    }
  }

  def shouldRemove(planCourse: PlanCourse): Boolean
}
