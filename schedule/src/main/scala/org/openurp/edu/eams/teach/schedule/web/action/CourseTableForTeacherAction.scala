package org.openurp.edu.eams.teach.schedule.web.action

import java.util.Arrays


import org.apache.commons.collections.CollectionUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.functor.Predicate
import org.openurp.base.Semester
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.teach.code.school.CourseHourType
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.CourseTableStyle
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategyFactory
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import org.openurp.edu.eams.teach.schedule.model.CourseArrangeSwitch
import org.openurp.edu.eams.teach.schedule.model.CourseTableSetting
import org.openurp.edu.eams.teach.schedule.util.CourseTable
import org.openurp.edu.eams.teach.service.TeachResourceService
import org.openurp.edu.eams.teach.web.action.AbstractTeacherLessonAction



class CourseTableForTeacherAction extends AbstractTeacherLessonAction {

  protected var lessonFilterStrategyFactory: LessonFilterStrategyFactory = _

  protected var lessonService: LessonService = _

  protected var timeSettingService: TimeSettingService = _

  protected var teachResourceService: TeachResourceService = _

  protected var majorPlanService: MajorPlanService = _

  def taskTable(): String = {
    val lessonId = getLongId("lesson")
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("error.teacher.notExists")
    }
    if (lesson == null || !lesson.getTeachers.contains(teacher)) {
      return forwardError("security.error.notEnoughAuthority")
    }
    put("startWeek", new java.lang.Integer(1))
    put("endWeek", new java.lang.Integer(lesson.getSemester.getWeeks))
    put("weekList", WeekDays.All)
    put("activityList", lesson.getCourseSchedule.getActivities)
    put("lesson", lesson)
    put("semester", lesson.getSemester)
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, lesson.getSemester, null))
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    forward()
  }

  def taskTableForTeacher(): String = {
    val lessonId = getLongId("lesson")
    if (lessonId != null) {
      val lesson = entityDao.get(classOf[Lesson], lessonId)
      val teacher = getLoginTeacher
      if (null == teacher) {
        return forwardError("error.teacher.notExists")
      }
      if (lesson == null || !lesson.getTeachers.contains(teacher)) {
        return forwardError("security.error.notEnoughAuthority")
      }
      put("endWeek", new java.lang.Integer(lesson.getSemester.getWeeks))
      put("activityList", lesson.getCourseSchedule.getActivities)
      put("semester", lesson.getSemester)
      put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, lesson.getSemester, null))
    } else {
      put("endWeek", 1)
      put("activityList", CollectionUtils.EMPTY_COLLECTION)
      put("semester", getSemester)
      put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, getSemesterService.getCurSemester(getProject), 
        null))
    }
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    put("startWeek", new java.lang.Integer(1))
    put("weekList", WeekDays.All)
    forward()
  }

  override def innerIndex(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("error.teacher.notExists")
    }
    putSemester(getProject)
    put("teacher", teacher)
    forward()
  }

  def courseTable(): String = {
    val semester = getSemester
    if (semester == null) {
      return forwardError("error.semester.id.notExists")
    }
    val query = OqlBuilder.from(classOf[CourseArrangeSwitch], "arrangeSwitch")
    query.where("arrangeSwitch.project =:project", getProject)
    query.where("arrangeSwitch.semester =:semester", semester)
    query.where("arrangeSwitch.published = true")
    if (CollectUtils.isEmpty(entityDao.search(query))) {
      return forwardError("当前学期课程安排还未发布")
    }
    val setting = populate(classOf[CourseTableSetting], "setting")
    setting.setSemester(semester)
    setting.setTimes(getTimesFormPage(semester))
    if (Strings.isEmpty(setting.getKind)) {
      return forwardError("error.courseTable.unknown")
    }
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("error.teacher.notExists")
    }
    setting.setWeekdays(Arrays.asList(WeekDays.All:_*))
    setting.setDisplaySemesterTime(true)
    put("courseTableList", CollectUtils.newArrayList(buildCourseTable(setting, teacher)))
    put("setting", setting)
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, setting.getSemester, null))
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward()
  }

  private def buildCourseTable(setting: CourseTableSetting, resource: Entity[_]): CourseTable = {
    val table = new CourseTable(resource, setting.getKind)
    var taskList = CollectUtils.newArrayList()
    val project = getProject
    if (CourseTable.TEACHER == setting.getKind) {
      val teacher = resource.asInstanceOf[Teacher]
      put("teacher", resource)
      for (j <- 0 until setting.getTimes.length) {
        val result = teachResourceService.getTeacherActivities(teacher, setting.getTimes()(j), if (setting.getForSemester) setting.getSemester else null)
        CollectUtils.filter(result, new Predicate[CourseActivity]() {

          def apply(input: CourseActivity): java.lang.Boolean = {
            return input.getLesson.getProject == project
          }
        })
        table.getActivities.addAll(result)
      }
      if (setting.getIgnoreTask) {
        return table
      }
      if (setting.getForSemester) {
        taskList = lessonService.getLessonByCategory(resource.id, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.TEACHER), 
          setting.getSemester)
        CollectUtils.filter(taskList, new Predicate[Lesson]() {

          def apply(input: Lesson): java.lang.Boolean = return input.getProject == project
        })
      } else {
        taskList = lessonService.getLessonByCategory(resource.id, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.TEACHER), 
          semesterService.getSemestersOfOverlapped(setting.getSemester))
      }
    }
    if (null == table.getLessons) table.setLessons(taskList)
    table
  }

  protected def getSemester(): Semester = {
    var semester = populate(classOf[Semester], "semester").asInstanceOf[Semester]
    val semesterId = getInt("semester.id")
    if (null != semesterId) {
      semester = entityDao.get(classOf[Semester], semesterId)
    }
    put("semester", semester)
    semester
  }

  protected def getTimesFormPage(semester: Semester): Array[CourseTime] = {
    var startWeek = getInt("startWeek")
    var endWeek = getInt("endWeek")
    if (null == startWeek) startWeek = new java.lang.Integer(1)
    if (null == endWeek) endWeek = new java.lang.Integer(semester.getWeeks)
    if (startWeek.intValue() < 1) startWeek = new java.lang.Integer(1)
    if (endWeek.intValue() > semester.getWeeks) endWeek = new java.lang.Integer(semester.getWeeks)
    put("startWeek", startWeek)
    put("endWeek", endWeek)
    Array(YearWeekTimeUtil.buildYearWeekTimes(2, startWeek.intValue(), endWeek.intValue(), CourseTime.CONTINUELY))
  }

  def printForm(): String = {
    val semester = getSemester
    if (semester == null) {
      return forwardError("error.semester.id.notExists")
    }
    val query = OqlBuilder.from(classOf[CourseArrangeSwitch], "arrangeSwitch")
    query.where("arrangeSwitch.project =:project", getProject)
    query.where("arrangeSwitch.semester =:semester", semester)
    query.where("arrangeSwitch.published = true")
    if (CollectUtils.isEmpty(entityDao.search(query))) {
      return forwardError("当前学期课程安排还未发布")
    }
    val setting = populate(classOf[CourseTableSetting], "setting")
    setting.setSemester(semester)
    setting.setTimes(getTimesFormPage(semester))
    if (Strings.isEmpty(setting.getKind)) {
      return forwardError("error.courseTable.unknown")
    }
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("error.teacher.notExists")
    }
    setting.setWeekdays(Arrays.asList(WeekDays.All:_*))
    setting.setDisplaySemesterTime(true)
    put("courseTableList", CollectUtils.newArrayList(buildCourseTable(setting, teacher)))
    put("setting", setting)
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, setting.getSemester, null))
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    forward()
  }

  def getLessonService(): LessonService = lessonService

  def setLessonService(lessonService: LessonService) {
    this.lessonService = lessonService
  }

  def getTimeSettingService(): TimeSettingService = timeSettingService

  def setTimeSettingService(timeSettingService: TimeSettingService) {
    this.timeSettingService = timeSettingService
  }

  def getTeachResourceService(): TeachResourceService = teachResourceService

  def setTeachResourceService(teachResourceService: TeachResourceService) {
    this.teachResourceService = teachResourceService
  }

  def getMajorPlanService(): MajorPlanService = majorPlanService

  def setMajorPlanService(majorPlanService: MajorPlanService) {
    this.majorPlanService = majorPlanService
  }

  def setLessonFilterStrategyFactory(lessonFilterStrategyFactory: LessonFilterStrategyFactory) {
    this.lessonFilterStrategyFactory = lessonFilterStrategyFactory
  }

  def printAttendanceCheckList(): String = {
    val lessonId = getLong("lesson.id")
    if (null == lessonId) {
      return forwardError("error.model.id.needed")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("error.teacher.notExists")
    }
    if (lesson == null || !lesson.getTeachers.contains(teacher)) {
      return forwardError("security.error.notEnoughAuthority")
    }
    val taskStdCollision = CollectUtils.newHashSet()
    val builder = OqlBuilder.from(classOf[CourseTake], "take")
    builder.join("take.lesson.courseSchedule.activities", "activity2")
    builder.where("take.std.id in (select std.id from " + classOf[CourseTake].getName + 
      " stdTake where stdTake.lesson = :lesson)", lesson)
    builder.where("take.lesson <> :lesson", lesson)
    builder.where("take.lesson.semester = :semester", lesson.getSemester)
    builder.where("exists(from " + classOf[CourseActivity].getName + " activity where activity.lesson = :lesson" + 
      " and bitand(activity.time.state, activity2.time.state) > 0" + 
      " and activity.time.day = activity2.time.day" + 
      " and activity.time.start <= activity2.time.end" + 
      " and activity2.time.start <= activity.time.end)", lesson)
    builder.select("select take.std.id")
    taskStdCollision.addAll(entityDao.search(builder).asInstanceOf[List[Long]])
    put("lesson", lesson)
    put("arrangeInfo", CourseActivityDigestor.getInstance.digest(getTextResource, lesson))
    put("courseTakes", lesson.getTeachClass.getCourseTakes)
    put("taskStdCollision", taskStdCollision)
    forward()
  }
}
