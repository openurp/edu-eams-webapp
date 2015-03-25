package org.openurp.edu.eams.teach.schedule.web.action

import java.io.IOException
import java.io.PrintWriter

import java.util.Arrays
import java.util.Comparator
import java.util.GregorianCalendar




import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Transformer
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.poi.ss.usermodel.DateUtil
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.collection.page.SinglePage
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.base.TimeSetting
import org.openurp.edu.eams.base.code.school.RoomType
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.eams.classroom.Occupancy
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.base.code.RoomUsage
import org.openurp.edu.eams.classroom.model.OccupancyBean
import org.openurp.edu.eams.classroom.service.RoomResourceService
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator.Usage
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.RoomService
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.teach.code.industry.TeachLangType
import org.openurp.edu.eams.teach.code.school.CourseHourType
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.eams.teach.lesson.CourseSchedule
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonGroup
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.model.CourseActivityBean
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.service.CourseLimitUtils
import org.openurp.edu.eams.teach.lesson.service.CourseTableStyle
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.eams.teach.schedule.log.ScheduleLogBuilder
import org.openurp.edu.eams.teach.schedule.model.AvailableTime
import org.openurp.edu.eams.teach.schedule.model.CollisionInfo
import org.openurp.edu.eams.teach.schedule.model.CollisionResource.ResourceType
import org.openurp.edu.eams.teach.schedule.service.CourseActivityService
import org.openurp.edu.eams.teach.schedule.service.ScheduleLogHelper
import org.openurp.edu.eams.teach.schedule.service.ScheduleRoomService
import org.openurp.edu.eams.teach.schedule.util.PropertyCollectionComparator
import org.openurp.edu.eams.teach.schedule.util.PropertyCollectionComparator.ArrangeOrder
import org.openurp.edu.eams.teach.service.TeachResourceService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import MultiManualArrangeAction._



object MultiManualArrangeAction {

  protected val ALLOWCONFLICT = "schedule.manualArrange.allowConflictOnPurpose"
}

class MultiManualArrangeAction extends SemesterSupportAction {

  var courseActivityService: CourseActivityService = _

  var lessonService: LessonService = _

  var classroomService: RoomService = _

  var lessonSearchHelper: LessonSearchHelper = _

  var teachResourceService: TeachResourceService = _

  var classroomResourceService: RoomResourceService = _

  var timeSettingService: TimeSettingService = _

  var scheduleLogHelper: ScheduleLogHelper = _

  var scheduleRoomService: ScheduleRoomService = _

  def setScheduleRoomService(scheduleRoomService: ScheduleRoomService) {
    this.scheduleRoomService = scheduleRoomService
  }

  private def isAllowConflictOnPurpose(): Boolean = {
    val allowConfilctStr = getConfig.get(ALLOWCONFLICT).asInstanceOf[String]
    if (Strings.isEmpty(allowConfilctStr)) false else java.lang.Boolean.valueOf(allowConfilctStr) || "1" == allowConfilctStr || 
      "是" == allowConfilctStr
  }

  def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    prepareDatas()
    forward()
  }

  def getFreeRoom() {
    val room = populate(classOf[Room], "classroom")
    val classroomCapacity = getInt("classroomCapacity")
    val classroomTypeId = getInt("classroomTypeId")
    if (null != classroomCapacity && room.getCapacity == 0) {
      room.setCapacity(classroomCapacity)
    }
    if (null != classroomTypeId && room.getType == null) {
      room.setType(Model.newInstance(classOf[RoomType], classroomTypeId))
    }
    val taskWeekStart = getInt("taskWeekStart")
    val year = getInt("year")
    val lastDay = year.toString + "-12-31"
    val gregorianCalendar = new GregorianCalendar()
    gregorianCalendar.setTime(DateUtil.parseYYYYMMDDDate(lastDay))
    var selectedWeekUnitSeq = get("selectedWeekUnits")
    selectedWeekUnitSeq = selectedWeekUnitSeq.replaceAll("<br>", "")
    val selectedWeeks = get("selectedWeeks")
    var vaildWeeks = Strings.repeat("0", 2 + taskWeekStart.intValue() - 2)
    vaildWeeks += selectedWeeks
    vaildWeeks += Strings.repeat("0", ExamYearWeekTimeUtil.OVERALLWEEKS - vaildWeeks.length)
    val timeList = new ArrayList[CourseTime]()
    val selectedWeeksUnits = selectedWeekUnitSeq.split(";")
    Arrays.sort(selectedWeeksUnits, new Comparator[String]() {

      def compare(arg0: String, arg1: String): Int = {
        return Numbers.toInt(Strings.remove(arg0, ',')) - Numbers.toInt(Strings.remove(arg1, ','))
      }
    })
    for (j <- 0 until selectedWeeksUnits.length) {
      val weekId = java.lang.Integer.valueOf(selectedWeeksUnits(j).substring(0, 1))
        .intValue()
      val unitId = java.lang.Integer.valueOf(selectedWeeksUnits(j).substring(2))
        .intValue()
      val newTime = new CourseTime()
      newTime.newWeekState(vaildWeeks)
      newTime.setWeekday(weekId)
      newTime.setStartUnit(unitId)
      newTime.setEndUnit(unitId)
      timeList.add(newTime)
    }
    val semesterIdForFreeRoom = getInt("semesterIdForFreeRoom")
    val project = entityDao.get(classOf[Project], getInt("projectId"))
    val semester = semesterService.getSemester(semesterIdForFreeRoom)
    val timeSetting = timeSettingService.getClosestTimeSetting(project, semester, null)
    for (j <- 0 until timeList.size) {
      val unit = timeList.get(j)
      unit.setStartTime(timeSetting.getDefaultUnits.get(unit.getStartUnit).start)
      unit.setEndTime(timeSetting.getDefaultUnits.get(unit.getEndUnit).end)
    }
    val courseSchedule = new CourseScheduleBean()
    courseSchedule.setWeekState(WeekStates.build(taskWeekStart + "-" + semester.getWeeks))
    val lesson = Model.newInstance(classOf[Lesson])
    lesson.setSemester(semester)
    lesson.setCourseSchedule(courseSchedule)
    val activity = Model.newInstance(classOf[CourseActivity])
    activity.setRooms(new HashSet[Room]())
    activity.getRooms.add(room)
    activity.setLesson(lesson)
    val builder = scheduleRoomService.getFreeRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
      activity)
      .orderBy("classroom.capacity,classroom.code")
    val classrooms = entityDao.search(builder)
    val response = getResponse
    response.setContentType("text/html")
    response.setCharacterEncoding("utf-8")
    if (CollectUtils.isNotEmpty(classrooms)) {
      try {
        val writer = response.getWriter
        val classroom = classrooms.get(0)
        writer.write(classroom.id.toString + "," + classroom.getName)
      } catch {
        case e: IOException => e.printStackTrace()
      }
    } else {
      try {
        val writer = response.getWriter
        writer.write("")
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
  }

  protected def prepareDatas() {
    val project = getProject
    val semesterId = getInt("semester.id")
    val semester = if (semesterId == null) getAttribute("semester").asInstanceOf[Semester] else entityDao.get(classOf[Semester], 
      semesterId)
    put("courseTypes", lessonService.courseTypesOfSemester(CollectUtils.newArrayList(project), getDeparts, 
      semester))
    put("teachDepartList", lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(project), getDeparts, 
      semester))
    val builder = OqlBuilder.from(classOf[Adminclass], "adminclass").where("adminclass.project = :project", 
      project)
      .where("adminclass.effectiveAt <= :now and (adminclass.invalidAt is null or adminclass.invalidAt >= :now)", 
      new java.util.Date())
      .orderBy("adminclass.grade desc")
    put("adminclasses", entityDao.search(builder))
    val groupBuilder = OqlBuilder.from(classOf[LessonGroup], "lessonGroup")
      .where("lessonGroup.project = :project", project)
      .where("lessonGroup.semester = :semester", semester)
      .orderBy("lessonGroup.name")
    put("lessonGroups", entityDao.search(groupBuilder))
    addBaseCode("languages", classOf[TeachLangType])
    put("courseStatusEnums", CourseStatusEnum.values)
    val status = get("status")
    if (Strings.isNotEmpty(status)) {
      put("arrangeStatus", status)
    }
    put("startWeekToEndWeeks", getStartWeekToEndWeeks(project, semester))
    val orderRule = get("orderRule")
    if (Strings.isNotEmpty(orderRule)) {
      put("orderRule", orderRule)
    }
    put("arrangeOrders", ArrangeOrder.values)
  }

  protected def getStartWeekToEndWeeks(project: Project, semester: Semester): List[_] = {
    val builderSchedule = OqlBuilder.from(classOf[Lesson], "lesson").where("lesson.project =:project", 
      project)
      .where("lesson.semester = :semester", semester)
      .select("distinct lesson.courseSchedule.startWeek,lesson.courseSchedule.endWeek")
      .orderBy("lesson.courseSchedule.startWeek,lesson.courseSchedule.endWeek")
    entityDao.search(builderSchedule)
  }

  def freeRoomList(): String = {
    val room = populate(classOf[Room], "classroom")
    val classroomCapacity = getInt("classroomCapacity")
    val classroomTypeId = getInt("classroomTypeId")
    val useFreeRoomParam = getBool("useFreeRoomParam")
    if (null != classroomCapacity && room.getCapacity == 0 && !useFreeRoomParam) {
      room.setCapacity(classroomCapacity)
    }
    if (null != classroomTypeId && room.getType == null && !useFreeRoomParam) {
      room.setType(Model.newInstance(classOf[RoomType], classroomTypeId))
    }
    val taskWeekStart = getInt("taskWeekStart")
    val year = getInt("year")
    val lastDay = year.toString + "-12-31"
    val gregorianCalendar = new GregorianCalendar()
    gregorianCalendar.setTime(DateUtil.parseYYYYMMDDDate(lastDay))
    var selectedWeekUnitSeq = get("selectedWeekUnits")
    selectedWeekUnitSeq = selectedWeekUnitSeq.replaceAll("<br>", "")
    val selectedWeeks = get("selectedWeeks")
    var vaildWeeks = Strings.repeat("0", 2 + taskWeekStart.intValue() - 2)
    vaildWeeks += selectedWeeks
    vaildWeeks += Strings.repeat("0", ExamYearWeekTimeUtil.OVERALLWEEKS - vaildWeeks.length)
    val timeList = new ArrayList[CourseTime]()
    val selectedWeeksUnits = selectedWeekUnitSeq.split(";")
    Arrays.sort(selectedWeeksUnits, new Comparator[String]() {

      def compare(arg0: String, arg1: String): Int = {
        return Numbers.toInt(Strings.remove(arg0, ',')) - Numbers.toInt(Strings.remove(arg1, ','))
      }
    })
    for (j <- 0 until selectedWeeksUnits.length) {
      val weekId = java.lang.Integer.valueOf(selectedWeeksUnits(j).substring(0, 1))
        .intValue()
      val unitId = java.lang.Integer.valueOf(selectedWeeksUnits(j).substring(2))
        .intValue()
      val newTime = new CourseTime()
      newTime.newWeekState(vaildWeeks)
      newTime.setWeekday(weekId)
      newTime.setStartUnit(unitId)
      newTime.setEndUnit(unitId)
      timeList.add(newTime)
    }
    val semesterIdForFreeRoom = getInt("semesterIdForFreeRoom")
    val project = entityDao.get(classOf[Project], getInt("projectId"))
    val semester = semesterService.getSemester(semesterIdForFreeRoom)
    val timeSetting = timeSettingService.getClosestTimeSetting(project, semester, null)
    for (j <- 0 until timeList.size) {
      val unit = timeList.get(j)
      unit.setStartTime(timeSetting.getDefaultUnits.get(unit.getStartUnit).start)
      unit.setEndTime(timeSetting.getDefaultUnits.get(unit.getEndUnit).end)
    }
    val courseSchedule = new CourseScheduleBean()
    courseSchedule.setWeekState(WeekStates.build(taskWeekStart + "-" + semester.getWeeks))
    val lesson = Model.newInstance(classOf[Lesson])
    lesson.setSemester(semester)
    lesson.setCourseSchedule(courseSchedule)
    val activity = Model.newInstance(classOf[CourseActivity])
    activity.setRooms(new HashSet[Room]())
    activity.getRooms.add(room)
    activity.setLesson(lesson)
    val builder = scheduleRoomService.getFreeRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
      activity)
      .orderBy("classroom.capacity,classroom.code")
    val classrooms = entityDao.search(builder)
    var pageNo = getPageNo
    val pageSize = getPageSize
    if ((classrooms.size / pageSize + 1) < pageNo) {
      pageNo = (classrooms.size / pageSize + 1)
    }
    val pageLastIndex = if (pageNo * pageSize > classrooms.size) classrooms.size else (pageNo * pageSize)
    put("classroomList", new SinglePage[Room](pageNo, pageSize, classrooms.size, classrooms.subList((pageNo - 1) * pageSize, 
      pageLastIndex)))
    put("configTypeList", baseCodeService.getCodes(classOf[RoomType]))
    put("pageSize", pageSize)
    put("classroom", room)
    forward()
  }

  override def search(): String = {
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    put("lessons", getLessons)
    put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, getSemester, null))
    put("weekList", WeekDays.All)
    put("semesterWeeks", getSemester.getWeeks)
    val showWeekend = getBoolean("showWeekend")
    put("showWeekend", if (null == showWeekend) false else showWeekend)
    var suggestRoom: java.lang.Boolean = null
    val cookies = getRequest.getCookies
    if (cookies != null) {
      for (i <- 0 until cookies.length if "suggestRoom" == cookies(i).getName) {
        suggestRoom = java.lang.Boolean.valueOf(cookies(i).getValue)
      }
    }
    put("suggestRoom", if (suggestRoom == null) false else suggestRoom)
    forward()
  }

  def prepareCookie() {
    val suggestRoom = if (null == getBoolean("suggestRoom")) false else getBoolean("suggestRoom")
    val response = getResponse
    val request = getRequest
    val cookies = request.getCookies
    var roomCookie = true
    if (cookies != null) {
      for (i <- 0 until cookies.length if "suggestRoom" == cookies(i).getName) {
        if (java.lang.Boolean.valueOf(cookies(i).getValue) != suggestRoom) {
          cookies(i).setValue(suggestRoom.toString)
          cookies(i).setPath(request.getContextPath)
          cookies(i).setMaxAge(365 * 24 * 60 * 60)
          response.addCookie(cookies(i))
        }
        roomCookie = false
      }
    }
    if (roomCookie) {
      val cookie = new Cookie("suggestRoom", if (null == suggestRoom) "false" else suggestRoom.toString)
      cookie.setPath(request.getContextPath)
      cookie.setMaxAge(365 * 24 * 60 * 60)
      response.addCookie(cookie)
    }
    put("suggestRoom", suggestRoom)
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.project.id=:projectid1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    populateConditions(builder, "lesson.id")
    restrictionHelper.applyRestriction(builder)
    val teacherId = getLong("teacher.id")
    var arrangeOrder: ArrangeOrder = null
    try {
      arrangeOrder = ArrangeOrder.valueOf(get("orderRule"))
    } catch {
      case e: Exception => arrangeOrder = ArrangeOrder.TEACHER
    }
    if (null != teacherId) {
      builder.join("lesson.teachers", "teacher")
      builder.where("teacher.id =:teacherId", teacherId)
    }
    val startWeekToEndWeek = get("startWeekToEndWeek")
    if (Strings.isNotEmpty(startWeekToEndWeek)) {
      var beginIndex = 0
      var index = 0
      val weeks = CollectUtils.newArrayList()
      while (index < startWeekToEndWeek.length) {
        if (startWeekToEndWeek.charAt(index) == '-') {
          if (index > beginIndex) {
            weeks.add(java.lang.Integer.valueOf(startWeekToEndWeek.substring(beginIndex, index)))
          }
          if (index > 0) {
            index += 1
          }
          beginIndex = index
          if (index < startWeekToEndWeek.length && startWeekToEndWeek.charAt(index) == '-') {
            index += 1
          }
        }
        index += 1
      }
      if (index > beginIndex) {
        weeks.add(java.lang.Integer.valueOf(startWeekToEndWeek.substring(beginIndex, index)))
      }
      builder.where("lesson.courseSchedule.startWeek = :startWeek", weeks.get(0))
        .where("lesson.courseSchedule.endWeek = :endWeek", weeks.get(1))
    }
    val occupancied = getBoolean("occupancied")
    if (null != occupancied) {
      if (true == occupancied) {
        builder.where("exists(from lesson.courseSchedule.activities activity where size(activity.rooms)>0)")
      } else {
        builder.where("exists(from lesson.courseSchedule.activities activity where size(activity.rooms)=0)")
      }
    }
    val adminclassId = getInt("adminclassId")
    if (null != adminclassId) {
      val con = CourseLimitUtils.build(entityDao.get(classOf[Adminclass], adminclassId), "lgi")
      val params = con.getParams
      builder.where("exists(from lesson.teachClass.limitGroups lg join lg.items as lgi where (lgi.operator='" + 
        Operator.EQUAL.name() + 
        "' or lgi.operator='" + 
        Operator.IN.name() + 
        "') and " + 
        con.getContent + 
        ")", params.get(0), params.get(1), params.get(2))
    }
    builder.where("lesson.project = :project", getProject)
    val isArrangeCompleted = get("status")
    if (Strings.isNotEmpty(isArrangeCompleted)) {
      if (isArrangeCompleted == CourseStatusEnum.NEED_ARRANGE.toString) {
        builder.where("lesson.courseSchedule.status = :status", CourseStatusEnum.NEED_ARRANGE)
      } else if (isArrangeCompleted == CourseStatusEnum.ARRANGED.toString) {
        builder.where("lesson.courseSchedule.status = :status", CourseStatusEnum.ARRANGED)
      }
    } else {
      builder.where("lesson.courseSchedule.status <> :status", CourseStatusEnum.DONT_ARRANGE)
    }
    val guapai = getBoolean("guapai")
    if (null != guapai) {
      if (true == guapai) {
        builder.join("lesson.tags", "tag")
        builder.where("tag.id = :guapaiId", LessonTag.PredefinedTags.GUAPAI.id)
      } else {
        builder.where("not exists (from lesson.tags tag where tag.id = :guapaiId)", LessonTag.PredefinedTags.GUAPAI.id)
      }
    }
    builder.where("lesson.auditStatus = :auditStatus", CommonAuditState.ACCEPTED)
    builder
  }

  protected def getSemester(): Semester = {
    val semesterId = getInt("lesson.semester.id")
    if (null == semesterId) {
      semesterService.getCurSemester(getProject)
    } else {
      entityDao.get(classOf[Semester], semesterId)
    }
  }

  protected def getLessons(): List[Lesson] = {
    val builder = getQueryBuilder
    val pageNo = getPageNo
    val pageSize = 100
    val pageLimit = new PageLimit(pageNo, pageSize)
    builder.limit(pageLimit)
    var arrangeOrder: ArrangeOrder = null
    try {
      arrangeOrder = ArrangeOrder.valueOf(get("orderRule"))
    } catch {
      case e: Exception => arrangeOrder = ArrangeOrder.TEACHER
    }
    if (ArrangeOrder.LESSONNO == arrangeOrder) {
      builder.orderBy("lesson.no")
    } else if (ArrangeOrder.COURSE == arrangeOrder) {
      builder.orderBy("lesson.course.name,lesson.no")
    }
    val lessons = entityDao.search(builder).asInstanceOf[List[Lesson]]
    if (ArrangeOrder.TEACHER == arrangeOrder) {
      Collections.sort(lessons, new PropertyCollectionComparator[Lesson](arrangeOrder, false))
    }
    put("currangeOrder", arrangeOrder)
    val lessonTeachers = CollectUtils.newHashMap()
    for (lesson <- lessons) {
      val teachers = lesson.getTeachers
      var teacherNames = ""
      for (i <- 0 until teachers.size) {
        if (i > 0) {
          teacherNames += ","
        }
        teacherNames += teachers.get(i).getName
      }
      lessonTeachers.put(lesson, teacherNames)
    }
    builder.clearOrders().limit(null).select("select count(*)")
    put("lessonPageNo", pageNo)
    val total = entityDao.search(builder).get(0).asInstanceOf[java.lang.Long]
    if (total > 100) {
      put("lessonPageFlag", true)
    }
    put("lessonTotal", (total / pageSize) + 1)
    put("lessonTeachers", lessonTeachers)
    lessons
  }

  def manualArrange(): String = {
    put("weekStates", new WeekStates())
    val changeStatus = getBool("changeStatus")
    val lessonId = getLongId("lesson")
    if (null == lessonId) {
      return forwardError("error.teachTask.notExists")
    }
    val lesson = entityDao.get(classOf[Lesson], getLongId("lesson"))
    if (null == lesson) return forwardError("error.teachTask.notExists")
    if (changeStatus) {
      put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
      put("timeSetting", timeSettingService.getClosestTimeSetting(getProject, getSemester, null))
      put("weekList", WeekDays.All)
      put("semesterWeeks", getSemester.getWeeks)
      val showWeekend = getBoolean("showWeekend")
      put("showWeekend", if (null == showWeekend) false else showWeekend)
      var suggestRoom: java.lang.Boolean = null
      val cookies = getRequest.getCookies
      if (cookies != null) {
        for (i <- 0 until cookies.length if "suggestRoom" == cookies(i).getName) {
          suggestRoom = java.lang.Boolean.valueOf(cookies(i).getValue)
        }
      }
      put("suggestRoom", if (suggestRoom == null) false else suggestRoom)
      put("changeStatus", changeStatus)
      put("lesson", lesson)
      return forward()
    }
    val taskActivities = lesson.getCourseSchedule.getActivities
    val time = YearWeekTimeUtil.buildYearWeekTimes(2, lesson.getCourseSchedule.getStartWeek, lesson.getCourseSchedule.getEndWeek, 
      CourseTime.CONTINUELY)
    val adminClasses = CollectUtils.newHashSet()
    val builder = OqlBuilder.from(classOf[CourseLimitItem], "courseLimitItem")
      .where("courseLimitItem.group.lesson=:lesson", lesson)
      .where("courseLimitItem.meta.id =:meta", CourseLimitMetaEnum.ADMINCLASS.getMetaId)
      .where("courseLimitItem.operator =:operator1 or courseLimitItem.operator =:operator2", Operator.IN, 
      Operator.EQUAL)
    val courseLimitItems = entityDao.search(builder)
    var adminClassIds = ""
    for (courseLimitItem <- courseLimitItems) {
      adminClassIds += courseLimitItem.getContent
    }
    val adminclassIdSeq = Strings.splitToInt(adminClassIds.replaceAll(",,", ","))
    if (ArrayUtils.isNotEmpty(adminclassIdSeq)) {
      adminClasses.addAll(entityDao.get(classOf[Adminclass], adminclassIdSeq))
    }
    val adminClassActivities = CollectUtils.newArrayList()
    for (adminClass <- adminClasses if Strings.isNotEmpty(time.state) && adminClass.isPersisted) {
      adminClassActivities.addAll(teachResourceService.getAdminclassActivities(adminClass, time, lesson.getSemester))
    }
    adminClassActivities.removeAll(taskActivities)
    val teacherActivities = new ArrayList[CourseActivity]()
    val availableTime = new AvailableTime()
    val timeSetting = timeSettingService.getClosestTimeSetting(getProject, lesson.getSemester, null)
    availableTime.setAvailable(Strings.repeat("1", WeekDays.MAX * timeSetting.getDefaultUnits.size))
    val teachers = lesson.getTeachers
    for (teacher <- teachers if Strings.isNotEmpty(time.state) && teacher.isPersisted) {
      teacherActivities.addAll(teachResourceService.getTeacherActivities(teacher, time, lesson.getSemester))
    }
    teacherActivities.removeAll(taskActivities)
    val classrooms = CollectUtils.newHashSet()
    for (activity <- taskActivities) {
      classrooms.addAll(activity.getRooms)
    }
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    put("classrooms", classrooms)
    put("availableTime", availableTime.getAvailable)
    put("adminclasses", adminClasses)
    put("adminClassActivities", adminClassActivities)
    put("teacherActivities", teacherActivities)
    put("taskActivities", lesson.getCourseSchedule.getActivities)
    put("weekList", WeekDays.All)
    put("lesson", lesson)
    put("timeSetting", timeSettingService.getClosestTimeSetting(lesson.getProject, lesson.getSemester, 
      lesson.getCampus))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    val showWeekend = getBoolean("showWeekend")
    put("showWeekend", if (null == showWeekend) true else showWeekend)
    prepareCookie()
    forward()
  }

  def saveActivitiesAjax(): String = {
    val lessonId = getLong("lessonId")
    prepareCookie()
    if (null == lessonId) {
      return forwardError("error.teachTask.id.needed")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val logUpdate = lesson.getCourseSchedule.getActivities.size > 0
    val forwardSearch = CourseStatusEnum.ARRANGED == lesson.getCourseSchedule.getStatus
    val count = getInt("activityCount")
    if (count.intValue() == 0) {
      try {
        courseActivityService.removeActivities(Array(lessonId), lesson.getSemester)
        scheduleLogHelper.log(ScheduleLogBuilder.delete(lesson, "多任务排课"))
        logHelper.info("Delete all Activity of lesson with id:" + lessonId)
      } catch {
        case e: Exception => {
          logHelper.info("Failure in deleting all Activity of lesson with id:" + 
            lessonId, e)
          return forwardError("error.occur")
        }
      }
    } else {
      val maxStdCount = getInt("maxStdCount")
      if (maxStdCount != null && maxStdCount.intValue() >= 0) {
        lesson.getTeachClass.setLimitCount(maxStdCount)
      }
      val activityList = CollectUtils.newArrayList(count.intValue())
      for (i <- 0 until count.intValue()) {
        val activity = populate(classOf[CourseActivity], "activity" + i)
        val teacherIds = Strings.splitToLong(get("teacherIds" + i))
        val roomIds = Strings.splitToInt(get("classroomIds" + i))
        if (ArrayUtils.isNotEmpty(teacherIds)) {
          activity.getTeachers.addAll(entityDao.get(classOf[Teacher], teacherIds))
        }
        if (ArrayUtils.isNotEmpty(roomIds)) {
          activity.getRooms.addAll(entityDao.get(classOf[Room], roomIds))
        }
        val weekState = get("activity" + i + ".time.state")
        if (Strings.isNotEmpty(weekState)) {
          activity.getTime.newWeekState(weekState)
        }
        activityList.add(activity)
      }
      val mergedActivityList = CourseActivityBean.mergeActivites(activityList)
      var detectCollision = getBoolean("detectCollision")
      if (null == detectCollision) {
        detectCollision = true
      }
      if (true == detectCollision) {
        val collisionTakes = courseActivityService.collisionTakes(lesson, mergedActivityList)
        if (CollectUtils.isNotEmpty(collisionTakes)) {
          put("courseTakes", collisionTakes)
          put("activities", mergedActivityList)
          put("lesson", lesson)
          put("saveAction", "!saveActivitiesAjax")
          put("courseActivityDigestor", CourseActivityDigestor.getInstance)
          put("resource", getTextResource)
          return forward("collisionStdList")
        }
      }
      val teacherNameCollector = new Transformer() {

        def transform(input: AnyRef): AnyRef = {
          return input.asInstanceOf[Teacher].getName
        }
      }
      val roomNameCollector = new Transformer() {

        def transform(input: AnyRef): AnyRef = {
          return input.asInstanceOf[Room].getName
        }
      }
      try {
        val beforeMsg = "Delete activities Before SAVE new activity and lesson No.:" + 
          lesson.getNo
        logHelper.info(beforeMsg)
        val lessonOccupancyRooms = CollectUtils.newHashSet()
        for (activity <- lesson.getCourseSchedule.getActivities; classroom <- activity.getRooms) {
          lessonOccupancyRooms.add(classroom)
        }
        val occupancies = CollectUtils.newHashSet()
        var period = 0
        val allowConflict = isAllowConflictOnPurpose
        for (activity <- mergedActivityList) {
          val timeSetting = timeSettingService.getClosestTimeSetting(lesson.getProject, lesson.getSemester, 
            lesson.getCampus)
          activity.getTime.setStartTime(timeSetting.getDefaultUnits.get(activity.getTime.getStartUnit)
            .start)
          activity.getTime.setEndTime(timeSetting.getDefaultUnits.get(activity.getTime.getEndUnit)
            .end)
          activity.setLesson(lesson)
          val time = activity.getTime
          val coursePeriod = (time.getEndUnit - time.getStartUnit + 1) * Strings.count(time.state, 
            "1")
          period += coursePeriod
          if (!allowConflict && 
            courseActivityService.isCourseActivityRoomOccupied(activity)) {
            val errMsg = new StringBuilder()
            errMsg.append(Strings.join(CollectionUtils.collect(activity.getRooms, roomNameCollector), 
              ","))
              .append(" 周")
              .append(activity.getTime.day)
              .append(" 第")
              .append(activity.getTime.getStartUnit)
              .append("小节-第")
              .append(activity.getTime.getEndUnit)
              .append("小节")
            return forwardError(Array(getText("lesson.courseSchedule.roomIsOccupied"), errMsg.toString))
          }
          if (!allowConflict && 
            courseActivityService.isCourseActivityTeacherOccupied(activity)) {
            val errMsg = new StringBuilder()
            errMsg.append(Strings.join(CollectionUtils.collect(activity.getTeachers, teacherNameCollector), 
              ","))
              .append(" 周")
              .append(activity.getTime.day)
              .append(" 第")
              .append(activity.getTime.getStartUnit)
              .append("小节-第")
              .append(activity.getTime.getEndUnit)
              .append("小节")
            return forwardError(Array("教师冲突", errMsg.toString))
          }
          val rooms = activity.getRooms
          val courseTime = activity.getTime
          val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(lesson, courseTime)
          val freeRooms = entityDao.search(scheduleRoomService.getFreeRoomsOfConditions(timeUnits))
          for (room <- rooms) {
            if (!allowConflict && !freeRooms.contains(room) && !lessonOccupancyRooms.contains(room)) {
              val errMsg = new StringBuilder()
              errMsg.append(room.getName + "被排考或者教室借用占用,请选择其他教室!")
              return forwardError(Array(getText("lesson.courseSchedule.roomIsOccupied"), errMsg.toString))
            }
            for (timeUnit <- timeUnits) {
              val occupancy = new OccupancyBean()
              occupancy.setRoom(room)
              occupancy.setUserid(RoomUseridGenerator.gen(lesson, Usage.COURSE))
              occupancy.setUsage(entityDao.get(classOf[RoomUsage], RoomUsage.COURSE))
              occupancy.setTime(timeUnit)
              occupancy.setComments(activity.getLesson.getNo + "[" + activity.getLesson.getCourse.getName + 
                "]")
              occupancies.add(occupancy)
            }
          }
        }
        lesson.getCourseSchedule.getActivities.clear()
        courseActivityService.removeActivities(Array(lesson.id), lesson.getSemester)
        lesson.getCourseSchedule.getActivities.addAll(mergedActivityList)
        lesson.getCourseSchedule.setPeriod(period)
        lesson.getCourseSchedule.setStatus(CourseStatusEnum.NEED_ARRANGE)
        entityDao.execute(Operation.saveOrUpdate(lesson).saveOrUpdate(occupancies))
        scheduleLogHelper.log(if (logUpdate) ScheduleLogBuilder.update(lesson, "多任务排课") else ScheduleLogBuilder.create(lesson, 
          "多任务排课"))
      } catch {
        case e: Exception => {
          e.printStackTrace()
          logHelper.info("Failure in deleting activities of lesson with id:" + 
            lessonId, e)
          return forwardError("error.occurred")
        }
      }
    }
    val forward = get("forward")
    val showWeekend = getBoolean("showWeekend")
    val params = get("params")
    if (Strings.isEmpty(forward)) {
      if (forwardSearch) {
        redirect("manualArrange", "info.save.success", "lesson.id=" + lessonId + "&showWeekend=" + (if (null == showWeekend) true else showWeekend) + 
          "&status=" + 
          get("status") + 
          "&lesson.semester.id=" + 
          lesson.getSemester.id + 
          params + 
          "&suggestRoom=" + 
          (if (null == getBoolean("suggestRoom")) false else getBoolean("suggestRoom")) + 
          "&changeStatus=1")
      } else {
        redirect("manualArrange", "info.save.success", "lesson.id=" + lessonId + "&showWeekend=" + (if (null == showWeekend) true else showWeekend) + 
          "&status=" + 
          get("status") + 
          "&lesson.semester.id=" + 
          lesson.getSemester.id + 
          params + 
          "&suggestRoom=" + 
          (if (null == getBoolean("suggestRoom")) false else getBoolean("suggestRoom")))
      }
    } else {
      addMessage("info.save.success")
      forward(forward)
    }
  }

  def saveActivities(): String = {
    val lessonId = getLong("lessonId")
    prepareCookie()
    if (null == lessonId) {
      return forwardError("error.teachTask.id.needed")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val logUpdate = lesson.getCourseSchedule.getActivities.size > 0
    val forwardSearch = CourseStatusEnum.NEED_ARRANGE == lesson.getCourseSchedule.getStatus
    val count = getInt("activityCount")
    if (count.intValue() == 0) {
      try {
        courseActivityService.removeActivities(Array(lessonId), lesson.getSemester)
        scheduleLogHelper.log(ScheduleLogBuilder.delete(lesson, "多任务排课"))
        logHelper.info("Delete all Activity of lesson with id:" + lessonId)
      } catch {
        case e: Exception => {
          logger.error(ExceptionUtils.getStackTrace(e))
          logHelper.info("Failure in deleting all Activity of lesson with id:" + 
            lessonId, e)
          return forwardError("error.occur")
        }
      }
    } else {
      val maxStdCount = getInt("maxStdCount")
      if (maxStdCount != null && maxStdCount.intValue() >= 0) {
        lesson.getTeachClass.setLimitCount(maxStdCount)
      }
      var alterationBefore = ""
      val activityList = CollectUtils.newArrayList(count.intValue())
      for (i <- 0 until count.intValue()) {
        val activity = populate(classOf[CourseActivity], "activity" + i)
        val teacherIds = Strings.splitToLong(get("teacherIds" + i))
        val roomIds = Strings.splitToInt(get("classroomIds" + i))
        if (ArrayUtils.isNotEmpty(teacherIds)) {
          activity.getTeachers.addAll(entityDao.get(classOf[Teacher], teacherIds))
        }
        if (ArrayUtils.isNotEmpty(roomIds)) {
          activity.getRooms.addAll(entityDao.get(classOf[Room], roomIds))
        }
        val weekState = get("activity" + i + ".time.state")
        if (Strings.isNotEmpty(weekState)) {
          activity.getTime.newWeekState(weekState)
        }
        activityList.add(activity)
      }
      val mergedActivityList = CourseActivityBean.mergeActivites(activityList)
      var detectCollision = getBoolean("detectCollision")
      if (null == detectCollision) {
        detectCollision = true
      }
      if (true == detectCollision) {
        val collisionTakes = courseActivityService.collisionTakes(lesson, mergedActivityList)
        if (CollectUtils.isNotEmpty(collisionTakes)) {
          put("courseTakes", collisionTakes)
          put("activities", mergedActivityList)
          put("lesson", lesson)
          put("saveAction", "!saveActivities")
          put("courseActivityDigestor", CourseActivityDigestor.getInstance)
          put("resource", getTextResource)
          return forward("collisionStdList")
        }
      }
      val teacherNameCollector = new Transformer() {

        def transform(input: AnyRef): AnyRef = {
          return input.asInstanceOf[Teacher].getName
        }
      }
      val roomNameCollector = new Transformer() {

        def transform(input: AnyRef): AnyRef = {
          return input.asInstanceOf[Room].getName
        }
      }
      try {
        val beforeMsg = "Delete activities Before SAVE new activity and lesson No.:" + 
          lesson.getNo
        logHelper.info(beforeMsg)
        alterationBefore = CourseActivityDigestor.getInstance.digest(null, lesson)
        val occupancies = CollectUtils.newHashSet()
        val lessonOccupancyRooms = CollectUtils.newHashSet()
        for (activity <- lesson.getCourseSchedule.getActivities; classroom <- activity.getRooms) {
          lessonOccupancyRooms.add(classroom)
        }
        var period = 0
        val allowConflict = isAllowConflictOnPurpose
        for (activity <- mergedActivityList) {
          val timeSetting = timeSettingService.getClosestTimeSetting(lesson.getProject, lesson.getSemester, 
            lesson.getCampus)
          activity.getTime.setStartTime(timeSetting.getDefaultUnits.get(activity.getTime.getStartUnit)
            .start)
          activity.getTime.setEndTime(timeSetting.getDefaultUnits.get(activity.getTime.getEndUnit)
            .end)
          activity.setLesson(lesson)
          val time = activity.getTime
          val coursePeriod = (time.getEndUnit - time.getStartUnit + 1) * Strings.count(time.state, 
            "1")
          period += coursePeriod
          if (!allowConflict && 
            courseActivityService.isCourseActivityRoomOccupied(activity)) {
            val errMsg = new StringBuilder()
            errMsg.append(Strings.join(CollectionUtils.collect(activity.getRooms, roomNameCollector), 
              ","))
              .append(" 周")
              .append(activity.getTime.day)
              .append(" 第")
              .append(activity.getTime.getStartUnit)
              .append("小节-第")
              .append(activity.getTime.getEndUnit)
              .append("小节")
            return forwardError(Array(getText("lesson.courseSchedule.roomIsOccupied"), errMsg.toString))
          }
          if (!allowConflict && 
            courseActivityService.isCourseActivityTeacherOccupied(activity)) {
            val errMsg = new StringBuilder()
            errMsg.append(Strings.join(CollectionUtils.collect(activity.getTeachers, teacherNameCollector), 
              ","))
              .append(" 周")
              .append(activity.getTime.day)
              .append(" 第")
              .append(activity.getTime.getStartUnit)
              .append("小节-第")
              .append(activity.getTime.getEndUnit)
              .append("小节")
            return forwardError(Array("教师冲突", errMsg.toString))
          }
          val rooms = activity.getRooms
          val courseTime = activity.getTime
          val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(lesson, courseTime)
          val freeRooms = entityDao.search(scheduleRoomService.getFreeRoomsOfConditions(timeUnits))
          for (room <- rooms) {
            if (!allowConflict && !freeRooms.contains(room) && !lessonOccupancyRooms.contains(room)) {
              val errMsg = new StringBuilder()
              errMsg.append(room.getName + "被排考或者教室借用占用,请选择其他教室!")
              return forwardError(Array(getText("lesson.courseSchedule.roomIsOccupied"), errMsg.toString))
            }
            for (timeUnit <- timeUnits) {
              val occupancy = new OccupancyBean()
              occupancy.setRoom(room)
              occupancy.setUserid(RoomUseridGenerator.gen(lesson, Usage.COURSE))
              occupancy.setUsage(entityDao.get(classOf[RoomUsage], RoomUsage.COURSE))
              occupancy.setTime(timeUnit)
              occupancy.setComments(activity.getLesson.getNo + "[" + activity.getLesson.getCourse.getName + 
                "]")
              occupancies.add(occupancy)
            }
          }
        }
        lesson.getCourseSchedule.getActivities.clear()
        courseActivityService.removeActivities(Array(lesson.id), lesson.getSemester)
        lesson.getCourseSchedule.getActivities.addAll(mergedActivityList)
        lesson.getCourseSchedule.setPeriod(period)
        courseActivityService.saveOrUpdateActivity(lesson, occupancies, alterationBefore, getBoolean("canToMessage"), 
          entityDao.get(classOf[User], getUserId), getRemoteAddr)
        scheduleLogHelper.log(if (logUpdate) ScheduleLogBuilder.update(lesson, "多任务排课") else ScheduleLogBuilder.create(lesson, 
          "多任务排课"))
      } catch {
        case e: Exception => {
          logger.error(ExceptionUtils.getStackTrace(e))
          logHelper.info("Failure in deleting activities of lesson with id:" + 
            lessonId, e)
          return forwardError("error.occurred")
        }
      }
    }
    val forward = get("forward")
    val showWeekend = getBoolean("showWeekend")
    val params = get("params")
    if (Strings.isEmpty(forward)) {
      if (forwardSearch) {
        redirect("manualArrange", "info.save.success", "lesson.id=" + lessonId + "&showWeekend=" + (if (null == showWeekend) true else showWeekend) + 
          "&status=" + 
          get("status") + 
          "&lesson.semester.id=" + 
          lesson.getSemester.id + 
          params + 
          "&suggestRoom=" + 
          (if (null == getBoolean("suggestRoom")) false else getBoolean("suggestRoom")) + 
          "&changeStatus=1")
      } else {
        redirect("manualArrange", "info.save.success", "lesson.id=" + lessonId + "&showWeekend=" + (if (null == showWeekend) true else showWeekend) + 
          "&status=" + 
          get("status") + 
          "&lesson.semester.id=" + 
          lesson.getSemester.id + 
          params + 
          "&suggestRoom=" + 
          (if (null == getBoolean("suggestRoom")) false else getBoolean("suggestRoom")))
      }
    } else {
      addMessage("info.save.success")
      forward(forward)
    }
  }

  def roomUtilizations(): String = {
    val `type` = getBool("type")
    if (`type`) {
      putElectCountRoomUtilizationOfCourse(getDeparts, entityDao.get(classOf[Semester], getInt("semester.id")), 
        getFloat("ratio"))
    } else {
      putRoomUtilizationOfCourse(getDeparts, entityDao.get(classOf[Semester], getInt("semester.id")), 
        getFloat("ratio"))
    }
    put("type", `type`)
    put("textResource", getTextResource)
    put("courseActivityDigestor", CourseActivityDigestor.getInstance)
    put("ratio", getFloat("ratio"))
    forward()
  }

  def putElectCountRoomUtilizationOfCourse(departments: List[Department], semester: Semester, ratio: java.lang.Float) {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
      .where("activity.lesson.semester=:semester", semester)
      .where("activity.lesson.teachDepart in (:depart)", departments)
      .where("activity.lesson.teachClass.stdCount * 1.0 / (select sum(capacity) from " + 
      classOf[Room].getName + 
      " room1 where exists(from activity.rooms room2 where room1=room2)) <= :ratio", ratio * 1d)
      .limit(getPageLimit)
      .orderBy("activity.lesson.no")
    val activitys = entityDao.search(builder)
    put("courseActivities", activitys)
    val utilizations = CollectUtils.newHashMap()
    for (courseActivity <- activitys) {
      val rooms = courseActivity.getRooms
      var capacity = 0
      for (room <- rooms) {
        capacity += room.getCapacity
      }
      val objs = Array.ofDim[Any](2)
      objs(1) = capacity
      if (capacity != 0) {
        val ratioNow = courseActivity.getLesson.getTeachClass.getStdCount.toFloat / 
          capacity.toFloat
        if (ratioNow <= ratio) {
          objs(0) = ratioNow
          utilizations.put(courseActivity, objs)
        }
      } else {
        objs(0) = 0f
        utilizations.put(courseActivity, objs)
      }
    }
    put("utilizations", utilizations)
  }

  def putRoomUtilizationOfCourse(departments: List[Department], semester: Semester, ratio: java.lang.Float) {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
      .where("activity.lesson.semester=:semester", semester)
      .where("activity.lesson.teachDepart in (:depart)", departments)
      .where("activity.lesson.teachClass.limitCount * 1.0 / (select sum(capacity) from " + 
      classOf[Room].getName + 
      " room1 where exists(from activity.rooms room2 where room1=room2)) <= :ratio", ratio * 1d)
      .limit(getPageLimit)
      .orderBy("activity.lesson.no")
    val activitys = entityDao.search(builder)
    put("courseActivities", activitys)
    val utilizations = CollectUtils.newHashMap()
    for (courseActivity <- activitys) {
      val rooms = courseActivity.getRooms
      var capacity = 0
      for (room <- rooms) {
        capacity += room.getCapacity
      }
      val objs = Array.ofDim[Any](2)
      objs(1) = capacity
      if (capacity != 0) {
        val ratioNow = courseActivity.getLesson.getTeachClass.getLimitCount.toFloat / 
          capacity.toFloat
        if (ratioNow <= ratio) {
          objs(0) = ratioNow
          utilizations.put(courseActivity, objs)
        }
      } else {
        objs(0) = 0f
        utilizations.put(courseActivity, objs)
      }
    }
    put("utilizations", utilizations)
  }

  def detectCollision(): String = {
    val semesterId = getInt("semester.id")
    if (null == semesterId) {
      return forwardError("errors.classElect.notSemester")
    }
    val semester = entityDao.get(classOf[Semester], semesterId)
    val kind = get("kind")
    var collisions: Iterable[CollisionInfo] = null
    val timeSetting = timeSettingService.getClosestTimeSetting(getProject, semester, null)
    if ("class" == kind) {
      collisions = courseActivityService.detectCollision(semester, ResourceType.ADMINCLASS, timeSetting)
    } else if ("room" == kind) {
      collisions = courseActivityService.detectCollision(semester, ResourceType.CLASSROOM, timeSetting)
    } else if ("teacher" == kind) {
      collisions = courseActivityService.detectCollision(semester, ResourceType.TEACHER, timeSetting)
    } else {
      throw new RuntimeException("unsurported collisioin detect type:" + kind)
    }
    put("collisions", collisions)
    forward()
  }

  def getDepertmentTeacher(): String = {
    val departmentId = getInt("departmentId")
    val semesterId = getInt("semesterId")
    if (null != departmentId) {
      val builder = OqlBuilder.from(classOf[Lesson], "lesson").join("lesson.teachers", "teacher")
        .where("lesson.teachDepart.id =:departmentId", departmentId)
        .select("select distinct teacher")
        .where("lesson.project =:project", getProject)
      if (null != semesterId) {
        builder.where("lesson.semester.id = :semesterId", semesterId)
      }
      put("teachers", entityDao.search(builder))
    } else {
      put("teachers", CollectionUtils.EMPTY_COLLECTION)
    }
    forward("departTeacher")
  }
}
