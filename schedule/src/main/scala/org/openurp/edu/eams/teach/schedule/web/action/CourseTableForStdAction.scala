package org.openurp.edu.eams.teach.schedule.web.action

import java.util.Arraysimport java.util.Date



import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.teach.Textbook
import org.openurp.edu.eams.teach.lesson.CourseMaterial
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.LessonMaterialBean
import org.openurp.edu.eams.teach.lesson.service.CourseTableStyle
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategyFactory
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import org.openurp.edu.eams.teach.schedule.model.CourseArrangeSwitch
import org.openurp.edu.eams.teach.schedule.model.CourseTableCheck
import org.openurp.edu.eams.teach.schedule.model.CourseTableSetting
import org.openurp.edu.eams.teach.schedule.service.StdCourseTablePermissionChecker
import org.openurp.edu.eams.teach.schedule.util.CourseTable
import org.openurp.edu.eams.teach.schedule.util.MultiCourseTable
import org.openurp.edu.eams.teach.service.TeachResourceService
import org.openurp.edu.eams.web.action.common.AbstractStudentProjectSupportAction



class CourseTableForStdAction extends AbstractStudentProjectSupportAction {

  var lessonFilterStrategyFactory: LessonFilterStrategyFactory = _

  var lessonService: LessonService = _

  var timeSettingService: TimeSettingService = _

  var teachResourceService: TeachResourceService = _

  var majorPlanService: MajorPlanService = _

  var stdCourseTablePermissionChecker: StdCourseTablePermissionChecker = _

  override def innerIndex(): String = {
    val std = getLoginStudent
    if (null == std) {
      return forwardError("error.std.stdNo.needed")
    }
    putSemester(getProject)
    put("std", std)
    forward()
  }

  def taskTable(): String = {
    val std = getLoginStudent
    if (null == std) {
      return forwardError("对不起,该功能只开放给学生用户!")
    }
    val lessonId = getLongId("lesson")
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    if (null != lesson) {
      val count = entityDao.count(classOf[CourseTake], Array("lesson", "std"), Array(lesson, std), null)
      if (count == 0) {
        return forwardError("没有权限")
      }
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

  def courseTable(): String = {
    val semester = getSemester
    if (semester == null) {
      return forwardError("error.semester.id.notExists")
    }
    val setting = populate(classOf[CourseTableSetting], "setting")
    setting.setForSemester(true)
    setting.setSemester(semester)
    setting.setTimes(getTimesFormPage(semester))
    if (Strings.isEmpty(setting.getKind)) {
      return forwardError("error.courseTable.unknown")
    }
    val ids = get("ids")
    if (Strings.isEmpty(ids)) {
      put("prompt", "common.lessOneSelectPlease")
      return forward("prompt")
    }
    val msg = stdCourseTablePermissionChecker.check(getLoginStudent, setting.getKind, ids)
    if (null != msg) {
      return forwardError(msg)
    }
    val clazz = CourseTable.getResourceClass(setting.getKind)
    val idClazz = Model.getType(clazz).idType
    val rsList = Collections.newBuffer[Any]
    for (a <- Strings.split(ids)) {
      rsList.add(DefaultConversion.Instance.convert(a, idClazz))
    }
    val entityQuery = OqlBuilder.from(clazz, "resource").where("resource.id in (:ids)", rsList)
    val resources = entityDao.search(entityQuery)
    val orders = Order.parse(get("setting.orderBy"))
    if (orders.isEmpty) {
      orders.add(new Order("code asc"))
    }
    val order = orders.get(0).asInstanceOf[Order]
    Collections.sort(resources, new PropertyComparator(getLastSubString(order.getProperty), order.isAscending))
    val courseTableList = Collections.newBuffer[Any]
    if (setting.getTablePerPage == 1) {
      for (resource <- resources) {
        courseTableList.add(buildCourseTable(setting, resource))
      }
    } else {
      var i = 0
      var multiTable: MultiCourseTable = null
      for (resource <- resources) {
        if (i % setting.getTablePerPage == 0) {
          multiTable = new MultiCourseTable()
          courseTableList.add(multiTable)
        }
        multiTable.getResources.add(resource)
        multiTable.getTables.add(buildCourseTable(setting, resource))
        i += 1
      }
    }
    setting.setWeekdays(Arrays.asList(WeekDays.All:_*))
    setting.setDisplaySemesterTime(true)
    put("courseTableList", courseTableList)
    if (setting.getTablePerPage == 1) {
      val textbookMap = Collections.newMap[Any]
      for (`object` <- courseTableList) {
        val table = `object`.asInstanceOf[CourseTable]
        for (lesson <- table.getLessons) {
          val lessonMaterials = entityDao.get(classOf[LessonMaterialBean], "lesson", lesson)
          if (!lessonMaterials.isEmpty) {
            val lessonMaterial = lessonMaterials.get(0)
            if (lessonMaterial.getPassed != null && true == lessonMaterial.getPassed) {
              textbookMap.put(lesson, Collections.newHashSet(lessonMaterials.get(0).getBooks))
            }
          } else {
            val courseMaterials = entityDao.search(OqlBuilder.from(classOf[CourseMaterial], "courseMaterial")
              .where("courseMaterial.course = :course", lesson.getCourse)
              .where("courseMaterial.department = :department", lesson.getTeachDepart)
              .where("courseMaterial.semester = :semester", lesson.getSemester)
              .where("courseMaterial.passed is true"))
            if (!courseMaterials.isEmpty) {
              textbookMap.put(lesson, Collections.newHashSet(courseMaterials.get(0).getBooks))
            }
          }
        }
      }
      put("textbookMap", textbookMap)
    }
    put("setting", setting)
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, setting.getSemester, null))
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    if (1 == setting.getTablePerPage) {
      forward()
    } else {
      forward("courseTable_" + setting.getStyle)
    }
  }

  private def buildCourseTable(setting: CourseTableSetting, resource: Entity[_]): CourseTable = {
    val table = new CourseTable(resource, setting.getKind)
    var taskList = Collections.newBuffer[Any]
    if (CourseTable.CLASS == setting.getKind) {
      for (j <- 0 until setting.getTimes.length) {
        table.getActivities.addAll(teachResourceService.getAdminclassActivities(resource.asInstanceOf[Adminclass], 
          setting.getTimes()(j), if (setting.getForSemester) setting.getSemester else null))
      }
      if (setting.getIgnoreTask) return table
      taskList = lessonService.getLessons(setting.getSemester, resource)
    } else if (CourseTable.STD == setting.getKind) {
      if (getLoginStudent != null) {
        val query = OqlBuilder.from(classOf[CourseArrangeSwitch], "switch")
        query.where("switch.semester = :semester", getSemester)
        query.where("switch.project = :project", getLoginStudent.getProject)
        query.where("switch.published is true")
        if (Collections.isEmpty(entityDao.search(query))) {
          if (null == table.getLessons) table.setLessons(taskList)
          return table
        }
      }
      val student = resource.asInstanceOf[Student]
      for (j <- 0 until setting.getTimes.length) {
        table.getActivities.addAll(teachResourceService.getStdActivities(student, setting.getTimes()(j), 
          if (setting.getForSemester) setting.getSemester else null))
      }
      if (setting.getIgnoreTask) {
        return table
      }
      taskList = if (setting.getForSemester) lessonService.getLessonByCategory(resource.id, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.STD), 
        setting.getSemester) else lessonService.getLessonByCategory(resource.id, lessonFilterStrategyFactory.getLessonFilterCategory(LessonFilterStrategy.STD), 
        semesterService.getSemestersOfOverlapped(setting.getSemester))
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

  protected def getLastSubString(str: String): String = {
    if (null == str) {
      return null
    }
    val subStrArr = Strings.split(str, ".")
    if (subStrArr.length > 0) {
      subStrArr(subStrArr.length - 1)
    } else {
      null
    }
  }

  def confirmCourseTable(): String = {
    val checkId = getLong("courseTableCheckId")
    if (null == checkId) {
      return forwardError("没有课表核对记录")
    }
    val check = entityDao.get(classOf[CourseTableCheck], checkId)
    val std = getLoginStudent
    if (check.getStd != std) {
      return forwardError("没有权限")
    }
    if (!check.isConfirm) {
      check.setConfirm(true)
      check.setConfirmAt(new Date(System.currentTimeMillis()))
      entityDao.saveOrUpdate(check)
    }
    redirect("innerIndex", "info.action.success")
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
}
