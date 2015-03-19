package org.openurp.edu.eams.teach.schedule.web.action



import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.convention.route.Action
import org.beangle.struts2.helper.ContextHelper
import org.openurp.base.Semester
import org.openurp.edu.eams.base.util.WeekDays
import org.openurp.edu.base.Project
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.teach.code.industry.TeachLangType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class CourseArrangeSettingAction extends SemesterSupportAction {

  private var lessonService: LessonService = _

  private var lessonSearchHelper: LessonSearchHelper = _

  private var timeSettingService: TimeSettingService = _

  def index(): String = {
    setSemesterDataRealm(hasStdType)
    val projectId = getInt("project.id")
    if (projectId != null) {
      ContextHelper.put("project", entityDao.get(classOf[Project], projectId))
    }
    val semesterId = getInt("semester.id")
    if (semesterId != null) {
      ContextHelper.put("semester", entityDao.get(classOf[Semester], semesterId))
    }
    val project = getProject
    put("courseTypes", lessonService.courseTypesOfSemester(CollectUtils.newArrayList(project), getDeparts, 
      getAttribute("semester").asInstanceOf[Semester]))
    put("teachDepartList", lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(project), getDeparts, 
      getAttribute("semester").asInstanceOf[Semester]))
    put("departmentList", lessonService.attendDepartsOfSemester(CollectUtils.newArrayList(project), getAttribute("semester").asInstanceOf[Semester]))
    put("stdTypeList", getStdTypes)
    addBaseCode("languages", classOf[TeachLangType])
    put("units", timeSettingService.getClosestTimeSetting(project, getAttribute("semester").asInstanceOf[Semester], 
      null)
      .getDefaultUnits
      .size)
    put("weeks", WeekDays.All)
    put("courseStatusEnums", CourseStatusEnum.values)
    val status = get("status")
    if (Strings.isEmpty(status)) {
      put("currentStatus", CourseStatusEnum.NEED_ARRANGE)
    } else {
      put("currentStatus", CourseStatusEnum.valueOf(status))
    }
    forward()
  }

  def taskList(): String = {
    val projectId = getInt("lesson.project.id")
    if (projectId != null) {
      put("project", entityDao.get(classOf[Project], projectId))
    }
    val semesterId = getInt("lesson.semester.id")
    if (semesterId != null) {
      put("semester", entityDao.get(classOf[Semester], semesterId))
    }
    val lessons = entityDao.search(getQueryBuilder)
    put("lessons", lessons)
    forward()
  }

  protected override def getQueryBuilder(): OqlBuilder[Lesson] = {
    val query = lessonSearchHelper.buildQuery()
    query.where("lesson.project=:project1", getProject)
    if (Strings.isEmpty(get(Order.ORDER_STR))) {
      query.orderBy("lesson.no")
    }
    put("teacherIsNull", getBool("fake.teacher.null"))
    val isArrangeCompleted = get("status")
    if (Strings.isNotEmpty(isArrangeCompleted)) {
      if (isArrangeCompleted == CourseStatusEnum.NEED_ARRANGE.toString) {
        query.where("lesson.courseSchedule.status = :status", CourseStatusEnum.NEED_ARRANGE)
        put("courseStatusEnum", CourseStatusEnum.NEED_ARRANGE)
      } else if (isArrangeCompleted == CourseStatusEnum.DONT_ARRANGE.toString) {
        query.where("lesson.courseSchedule.status = :status", CourseStatusEnum.DONT_ARRANGE)
        put("courseStatusEnum", CourseStatusEnum.DONT_ARRANGE)
      } else if (isArrangeCompleted == CourseStatusEnum.ARRANGED.toString) {
        query.where("lesson.courseSchedule.status = :status", CourseStatusEnum.ARRANGED)
        put("courseStatusEnum", CourseStatusEnum.ARRANGED)
      }
    }
    query
  }

  def changeStatus(): String = {
    val lessonIds = getLongIds("lesson")
    val semesterId = getInt("lesson.semester.id")
    val status = get("status")
    if (ArrayUtils.isEmpty(lessonIds)) {
      return forward(new Action("", "taskList"), "error.teachTask.id.needed")
    }
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    val courseStatusEnum = if ("NEED_ARRANGE" == status) CourseStatusEnum.DONT_ARRANGE else CourseStatusEnum.NEED_ARRANGE
    for (lesson <- lessons) {
      lesson.getCourseSchedule.setStatus(courseStatusEnum)
    }
    try {
      entityDao.saveOrUpdate(lessons)
    } catch {
      case e: Exception => return redirect("taskList", "info.action.failure", "status=" + status + "&lesson.semester.id=" + semesterId)
    }
    redirect("taskList", "info.action.success", "status=" + status + "&lesson.semester.id=" + semesterId)
  }

  def setLessonService(lessonService: LessonService) {
    this.lessonService = lessonService
  }

  def setLessonSearchHelper(lessonSearchHelper: LessonSearchHelper) {
    this.lessonSearchHelper = lessonSearchHelper
  }

  def setTimeSettingService(timeSettingService: TimeSettingService) {
    this.timeSettingService = timeSettingService
  }
}
