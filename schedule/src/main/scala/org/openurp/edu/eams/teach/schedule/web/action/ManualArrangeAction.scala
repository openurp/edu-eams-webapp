package org.openurp.edu.eams.teach.schedule.web.action


import java.util.Arrays
import java.util.Calendar
import java.util.Comparator
import java.util.GregorianCalendar




import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Transformer
import org.apache.commons.lang3.ArrayUtils
import org.apache.poi.ss.usermodel.DateUtil
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.BitStrings
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.Message
import org.beangle.security.blueprint.User
import org.beangle.struts2.convention.route.Action
import org.openurp.edu.eams.base.Building
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.base.TimeSetting
import org.openurp.edu.eams.base.code.school.RoomType
import org.openurp.edu.eams.base.util.WeekDays
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.eams.classroom.Occupancy
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.base.code.RoomUsage
import org.openurp.edu.eams.classroom.model.OccupancyBean
import org.openurp.edu.eams.classroom.service.RoomResourceService
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator.Usage
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.RoomService
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.teach.code.industry.TeachLangType
import org.openurp.edu.eams.teach.lesson.ArrangeSuggest
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.eams.teach.lesson.CourseSchedule
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.model.CourseActivityBean
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.service.CourseTableStyle
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.base.Program
import org.openurp.edu.eams.teach.schedule.log.ScheduleLogBuilder
import org.openurp.edu.eams.teach.schedule.model.AvailableTime
import org.openurp.edu.eams.teach.schedule.model.CollisionInfo
import org.openurp.edu.eams.teach.schedule.model.CollisionResource.ResourceType
import org.openurp.edu.eams.teach.schedule.service.CourseActivityService
import org.openurp.edu.eams.teach.schedule.service.LessonScheduleChecker
import org.openurp.edu.eams.teach.schedule.service.ScheduleLogHelper
import org.openurp.edu.eams.teach.schedule.service.ScheduleRoomService
import org.openurp.edu.eams.teach.service.TeachResourceService
import org.openurp.edu.eams.teach.workload.service.TeacherPeriodLimitService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import org.openurp.edu.eams.web.action.util.DataAuthorityUtil
import ManualArrangeAction._



object ManualArrangeAction {

  protected val ALLOWCONFLICT = "schedule.manualArrange.allowConflictOnPurpose"
}

class ManualArrangeAction extends SemesterSupportAction {

  protected var courseActivityService: CourseActivityService = _

  protected var lessonService: LessonService = _

  protected var classroomService: RoomService = _

  protected var lessonSearchHelper: LessonSearchHelper = _

  protected var teachResourceService: TeachResourceService = _

  protected var classroomResourceService: RoomResourceService = _

  protected var scheduleRoomService: ScheduleRoomService = _

  protected var timeSettingService: TimeSettingService = _

  protected var scheduleLogHelper: ScheduleLogHelper = _

  protected var checkers: List[LessonScheduleChecker] = CollectUtils.newArrayList()

  protected var teacherPeriodLimitService: TeacherPeriodLimitService = _

  protected var courseLimitService: CourseLimitService = _

  protected def getQueryBuilder(): OqlBuilder[Lesson] = {
    val query = lessonSearchHelper.buildQuery()
    query.where("lesson.project.id = :projectId1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    if (Strings.isEmpty(get(Order.ORDER_STR))) {
      query.orderBy("lesson.no")
    }
    val isArrangeCompleted = get("status")
    put("teacherIsNull", getBool("fake.teacher.null"))
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

  def taskList(): String = {
    val lessons = entityDao.search(getQueryBuilder)
    put("project", getProject)
    put("semester", putSemester(null))
    put("lessons", lessons)
    val digestor = CourseActivityDigestor.getInstance.setDelimeter("<br>")
    val arrangeInfo = CollectUtils.newHashMap()
    for (oneTask <- lessons) {
      arrangeInfo.put(oneTask.id.toString, digestor.digest(getTextResource, oneTask, ":teacher+ :day :units :weeks :room"))
    }
    put("arrangeInfo", arrangeInfo)
    put("weekStates", new WeekStates())
    forward()
  }

  def adminArrangeStatus(): String = {
    val lessonIds = getLongIds("lesson")
    val semester = putSemester(null)
    val adminClasses = CollectUtils.newHashSet()
    val builder = OqlBuilder.from(classOf[CourseLimitItem], "courseLimitItem")
      .where("courseLimitItem.group.lesson.id in (:lessonIds)", lessonIds)
      .where("courseLimitItem.meta.id =:meta", CourseLimitMetaEnum.ADMINCLASS.getMetaId)
      .where("courseLimitItem.operator =:operator1 or courseLimitItem.operator =:operator2", Operator.IN, 
      Operator.EQUAL)
    val courseLimitItems = entityDao.search(builder)
    var adminClassIds = ""
    for (courseLimitItem <- courseLimitItems) {
      adminClassIds += if (courseLimitItem.getContent.indexOf(",") > -1) courseLimitItem.getContent else courseLimitItem.getContent + ","
    }
    val adminclassIdSeq = Strings.splitToInt(adminClassIds.replaceAll(",,", ","))
    if (ArrayUtils.isNotEmpty(adminclassIdSeq)) {
      adminClasses.addAll(entityDao.get(classOf[Adminclass], adminclassIdSeq))
    }
    val time = YearWeekTimeUtil.buildYearWeekTimes(2, 1, semester.getWeeks, CourseTime.CONTINUELY)
    val adminClassActivities = CollectUtils.newHashMap()
    val courseSchedule = new CourseScheduleBean()
    courseSchedule.setWeekState(WeekStates.build("1-" + semester.getWeeks))
    val digestor = CourseActivityDigestor.getInstance
    for (adminclass <- adminClasses) {
      val activities = teachResourceService.getAdminclassActivities(adminclass, time, semester)
      if (activities.isEmpty) {
        adminClassActivities.put(adminclass, "无排课活动")
      } else {
        adminClassActivities.put(adminclass, digestor.digest(getTextResource, activities, ":course :teacher+ :day :units :weeks :room"))
      }
    }
    put("adminClassActivities", adminClassActivities)
    forward()
  }

  def index(): String = {
    setSemesterDataRealm(hasStdType)
    val project = getProject
    put("courseTypes", lessonService.courseTypesOfSemester(CollectUtils.newArrayList(project), getDeparts, 
      getAttribute("semester").asInstanceOf[Semester]))
    put("teachDepartList", lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(project), getDeparts, 
      getAttribute("semester").asInstanceOf[Semester]))
    put("departmentList", getCollegeOfDeparts)
    put("stdTypeList", getStdTypes)
    addBaseCode("languages", classOf[TeachLangType])
    put("weeks", WeekDays.All)
    val setting = timeSettingService.getClosestTimeSetting(project, getAttribute("semester").asInstanceOf[Semester], 
      null)
    put("units", if (setting == null) 0 else setting.getDefaultUnits.size)
    put("courseStatusEnums", CourseStatusEnum.values)
    val status = get("status")
    if (Strings.isEmpty(status)) {
      put("currentStatus", CourseStatusEnum.NEED_ARRANGE)
    } else {
      put("currentStatus", CourseStatusEnum.valueOf(status))
    }
    forward()
  }

  protected def checkSchedule(lesson: Lesson, activities: List[CourseActivity]): List[Message] = {
    val messages = CollectUtils.newArrayList()
    for (checker <- checkers) {
      val message = checker.check(lesson, activities)
      if (null != message) {
        messages.add(message)
      }
    }
    messages
  }

  def saveActivities(): String = {
    val lessonId = getLong("lessonId")
    val allowConfilctStr = getConfig.get(ALLOWCONFLICT).asInstanceOf[String]
    val allowConflict = if (Strings.isEmpty(allowConfilctStr)) false else java.lang.Boolean.valueOf(allowConfilctStr) || "1" == allowConfilctStr || 
      "是" == allowConfilctStr
    if (null == lessonId) {
      return forwardError("error.teachTask.id.needed")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val logUpdate = lesson.getCourseSchedule.getActivities.size > 0
    val count = getInt("activityCount")
    if (count.intValue() == 0) {
      if (lesson.getCourseSchedule.getActivities.isEmpty) {
        return redirect("taskList", "未进行排课操作", "status=" + CourseStatusEnum.NEED_ARRANGE + "&lesson.semester.id=" + 
          lesson.getSemester.id)
      }
      try {
        courseActivityService.removeActivities(Array(lessonId), lesson.getSemester)
        scheduleLogHelper.log(ScheduleLogBuilder.delete(lesson, "手工排课"))
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
          if (!allowConflict) {
            if (courseActivityService.isCourseActivityRoomOccupied(activity)) {
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
            if (courseActivityService.isCourseActivityTeacherOccupied(activity)) {
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
          }
          val rooms = activity.getRooms
          val courseTime = activity.getTime
          val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(lesson, courseTime)
          val freeRooms = entityDao.search(scheduleRoomService.getFreeRoomsOfConditions(timeUnits))
          for (room <- rooms) {
            if (!allowConflict) {
              if (!freeRooms.contains(room) && !lessonOccupancyRooms.contains(room)) {
                val errMsg = new StringBuilder()
                errMsg.append(room.getName + "被排考或者教室借用占用,请选择其他教室!")
                return forwardError(Array(getText("lesson.courseSchedule.roomIsOccupied"), errMsg.toString))
              }
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
        val messages = checkSchedule(lesson, mergedActivityList)
        if (!messages.isEmpty) {
          return redirect("taskList", getText(messages.get(0).getKey, messages.get(0).getKey, messages.get(0).getParams), 
            "status=" + CourseStatusEnum.NEED_ARRANGE + "&lesson.semester.id=" + 
            lesson.getSemester.id)
        }
        lesson.getCourseSchedule.getActivities.clear()
        courseActivityService.removeActivities(Array(lesson.id), lesson.getSemester)
        lesson.getCourseSchedule.getActivities.addAll(mergedActivityList)
        lesson.getCourseSchedule.setPeriod(period)
        courseActivityService.saveOrUpdateActivity(lesson, occupancies, alterationBefore, getBoolean("canToMessage"), 
          entityDao.get(classOf[User], getUserId), getRemoteAddr)
        scheduleLogHelper.log(if (logUpdate) ScheduleLogBuilder.update(lesson, "手工排课") else ScheduleLogBuilder.create(lesson, 
          "手工排课"))
      } catch {
        case e: Exception => {
          e.printStackTrace()
          logHelper.info("Failure in deleting activities of lesson with id:" + 
            lessonId, e)
          return forwardError("error.occurred")
        }
      }
    }
    val srcAction = get("srcAction")
    if (Strings.isNotEmpty(srcAction)) {
      return redirect(new Action(srcAction, "taskList"), "info.save.success")
    }
    val forward = get("forward")
    if (Strings.isEmpty(forward)) {
      redirect("taskList", "info.save.success", "status=" + CourseStatusEnum.NEED_ARRANGE + "&lesson.semester.id=" + 
        lesson.getSemester.id)
    } else {
      addMessage("info.save.success")
      forward(forward)
    }
  }

  def detectTaskCollision(): String = {
    val semesterId = getInt("lesson.semester.id")
    if (null == semesterId) {
      return forwardError("没有学年学期")
    }
    val semester = entityDao.get(classOf[Semester], semesterId)
    val lessonIds = getLongIds("lesson")
    val timeSetting = timeSettingService.getClosestTimeSetting(getProject, semester, null)
    val lessons = CollectUtils.newArrayList()
    val classCollisMap = CollectUtils.newHashMap()
    val roomCollisMap = CollectUtils.newHashMap()
    val teacherCollisMap = CollectUtils.newHashMap()
    for (i <- 0 until lessonIds.length) {
      val lessonId = lessonIds(i)
      val classCollisions = courseActivityService.detectCollision(semester, ResourceType.ADMINCLASS, 
        timeSetting, lessonId)
      val roomCollisions = courseActivityService.detectCollision(semester, ResourceType.CLASSROOM, timeSetting, 
        lessonId)
      val teacherCollisions = courseActivityService.detectCollision(semester, ResourceType.TEACHER, timeSetting, 
        lessonId)
      val lesson = entityDao.get(classOf[Lesson], lessonId)
      lessons.add(lesson)
      classCollisMap.put(lesson, classCollisions)
      roomCollisMap.put(lesson, roomCollisions)
      teacherCollisMap.put(lesson, teacherCollisions)
    }
    put("lessons", lessons)
    put("classCollisMap", classCollisMap)
    put("roomCollisMap", roomCollisMap)
    put("teacherCollisMap", teacherCollisMap)
    put("semester", semester)
    forward()
  }

  def shift(): String = {
    val semesterId = getInt("lesson.semester.id")
    if (null == semesterId) {
      return forwardError("没有学年学期")
    }
    var offset = 0
    try {
      offset = java.lang.Integer.parseInt(get("offset"))
    } catch {
      case e: Exception => return forwardError("输入的周数过长或不是整数")
    }
    if (offset == 0) {
      return forwardError("没有平移周数或平移周数等于0")
    }
    val lessonIds = getLongIds("lesson")
    val semester = entityDao.get(classOf[Semester], semesterId)
    if (null != offset && ArrayUtils.isNotEmpty(lessonIds)) {
      val lessons = entityDao.get(classOf[Lesson], lessonIds)
      for (lesson <- lessons if (lesson.getCourseSchedule.getStartWeek + offset) > semester.getWeeks || 
        (lesson.getCourseSchedule.getEndWeek + offset) < 0) {
        return forwardError(lesson.getNo + "平移教学周后排课时间不存在")
      }
      for (lesson <- lessons) {
        try {
          courseActivityService.shift(lesson, offset.intValue(), getBoolean("canToMessage"), entityDao.get(classOf[User], 
            getUserId))
          scheduleLogHelper.log(ScheduleLogBuilder.update(lesson, "平移教学周:" + offset + "周"))
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
    redirect("detectTaskCollision", "manualArrange.shift.success", "lesson.semester.id=" + semesterId + "&lessonIds=" + get("lessonIds"))
  }

  def removeArrangeResult(): String = {
    val lessonIds = getLongIds("lesson")
    val semesterId = getInt("semester.id")
    if (ArrayUtils.isEmpty(lessonIds) || semesterId == null) {
      return forward(new Action("", "taskList"), "error.teachTask.id.needed")
    }
    try {
      courseActivityService.removeActivities(lessonIds, entityDao.get(classOf[Semester], semesterId))
      for (lessonId <- lessonIds) {
        scheduleLogHelper.log(ScheduleLogBuilder.delete(entityDao.get(classOf[Lesson], lessonId), "手工排课"))
      }
    } catch {
      case e: Exception => return redirect("taskList", "info.delete.failure", "status=" + CourseStatusEnum.ARRANGED + "&lesson.semester.id=" + 
        semesterId)
    }
    redirect("taskList", "info.delete.success", "status=" + CourseStatusEnum.ARRANGED + "&lesson.semester.id=" + 
      semesterId)
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

  def taskSettingInArrange(): String = {
    val lesson = populateEntity(classOf[Lesson], "lesson")
    val scheduleWeekState = get("scheduleWeekState")
    if (null != scheduleWeekState && null != lesson.getCourseSchedule) {
      lesson.getCourseSchedule.setWeekState(WeekStates.build(scheduleWeekState))
    }
    setTeachersAndRooms(get("cteacherIds"), lesson)
    entityDao.saveOrUpdate(lesson)
    getFlash.put("params", get("params"))
    redirect("manualArrange", "", "lesson.id=" + get("lesson.id"))
  }

  protected def setTeachersAndRooms(teacherIdSeq: String, lesson: Lesson) {
    lesson.getTeachers.clear()
    if (Strings.isNotEmpty(teacherIdSeq)) {
      lesson.getTeachers.addAll(entityDao.get(classOf[Teacher], Strings.splitToLong(teacherIdSeq)))
    }
  }

  def manualArrange(): String = {
    val lesson = entityDao.get(classOf[Lesson], getLong("lesson.id"))
    if (null == lesson) return forwardError("error.teachTask.notExists")
    val taskActivities = lesson.getCourseSchedule.getActivities
    val time = YearWeekTimeUtil.buildYearWeekTimes(2, lesson.getCourseSchedule.getStartWeek, lesson.getCourseSchedule.getEndWeek, 
      CourseTime.CONTINUELY)
    val adminClasses = courseLimitService.extractAdminclasses(lesson.getTeachClass)
    val adminClassActivities = CollectUtils.newArrayList()
    for (adminClass <- adminClasses if Strings.isNotEmpty(time.state) && adminClass.isPersisted) {
      adminClassActivities.addAll(teachResourceService.getAdminclassActivities(adminClass, time, lesson.getSemester))
    }
    adminClassActivities.removeAll(taskActivities)
    val programs = courseLimitService.extractPrograms(lesson.getTeachClass)
    val programActivities = CollectUtils.newArrayList()
    for (program <- programs if Strings.isNotEmpty(time.state) && program.isPersisted) {
      programActivities.addAll(teachResourceService.getProgramActivities(program, time, lesson.getSemester))
    }
    programActivities.removeAll(taskActivities)
    val timeSetting = timeSettingService.getClosestTimeSetting(getProject, lesson.getSemester, null)
    val teacherActivities = new ArrayList[CourseActivity]()
    val availableTime = new AvailableTime()
    availableTime.setAvailable(Strings.repeat("1", WeekDays.MAX * timeSetting.getDefaultUnits.size))
    val teachers = lesson.getTeachers
    val teacherPeriod = CollectUtils.newHashMap()
    for (teacher <- teachers) {
      if (teacherPeriodLimitService != null) {
        val period = teacherPeriodLimitService.getMaxPeriod(teacher)
        teacherPeriod.put(teacher, "上限:" + 
          (if (java.lang.Integer.MAX_VALUE == period) "无" else period) + 
          ";已排:" + 
          teacherPeriodLimitService.getTeacherPeriods(teacher, lesson.getSemester))
      }
      if (Strings.isNotEmpty(time.state) && teacher.isPersisted) {
        teacherActivities.addAll(teachResourceService.getTeacherActivities(teacher, time, lesson.getSemester))
      }
    }
    put("teacherPeriod", teacherPeriod)
    teacherActivities.removeAll(taskActivities)
    put("availableTime", availableTime.getAvailable)
    put("adminClassActivities", adminClassActivities)
    put("programActivities", programActivities)
    put("teacherActivities", teacherActivities)
    put("taskActivities", lesson.getCourseSchedule.getActivities)
    put("weekList", WeekDays.All)
    put("lesson", lesson)
    put("timeSetting", timeSettingService.getClosestTimeSetting(lesson.getProject, lesson.getSemester, 
      lesson.getCampus))
    put("teacherDepart", getDeparts.get(0))
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    val suggests = entityDao.get(classOf[ArrangeSuggest], "lesson", lesson)
    val query = OqlBuilder.from(classOf[Department], "department")
    val hql = "exists (from org.openurp.edu.base.Teacher teacher where teacher.department = department and teacher.teaching = true)"
    query.where(hql)
    put("departmentList", entityDao.search(query))
    put("srcAction", get("srcAction"))
    put("weekStates", new WeekStates())
    forward()
  }

  def freeRoomList(): String = {
    var detectCollision = getBoolean("detectCollision")
    if (null == detectCollision) {
      detectCollision = true
    }
    val room = populate(classOf[Room], "classroom")
    val departments = getDeparts
    if (false == detectCollision) {
      put("classroomList", scheduleRoomService.getRooms(room, departments, getPageLimit))
    } else {
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
      vaildWeeks += Strings.repeat("0", Semester.OVERALLWEEKS - vaildWeeks.length)
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
      val project = getProject
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
      val confilctRoom = getBool("confilctRoom")
      var builder: OqlBuilder[Room] = null
      builder = if (confilctRoom) scheduleRoomService.getOccupancyRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
        activity)
        .orderBy("classroom.capacity,classroom.code")
        .limit(getPageLimit) else scheduleRoomService.getFreeRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
        activity)
        .orderBy("classroom.capacity,classroom.code")
        .limit(getPageLimit)
      put("classroomList", entityDao.search(builder))
      put("confilctRoom", confilctRoom)
    }
    if (room.getCampus != null && room.getCampus.isPersisted) {
      put("buildingList", CollectionUtils.intersection(baseInfoService.getBaseInfos(classOf[Building]), 
        entityDao.get(classOf[Building], "campus", room.getCampus)))
    } else {
      put("buildingList", Collections.EMPTY_LIST)
    }
    put("campusList", getProject.getCampuses)
    put("configTypeList", baseCodeService.getCodes(classOf[RoomType]))
    put("detectCollision", detectCollision)
    if (getLong("classroom.campus.id") != null) {
      val query = OqlBuilder.from(classOf[Building], "building")
      query.where("building.campus.id =:campusId", getInt("classroom.campus.id"))
      query.orderBy("building.code")
      put("buildings", entityDao.search(query))
    }
    forward()
  }

  def shiftSemester(): String = {
    val offset = getInt("offset")
    val semesterId = getInt("semester.id")
    if (null == semesterId) {
      return forwardError("errors.classElect.notSemester")
    }
    val semester = entityDao.get(classOf[Semester], semesterId)
    try {
      if (offset.intValue() != 0) {
        val c = Calendar.getInstance
        c.setTime(semester.beginOn)
        c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + offset.intValue() * 7)
        semester.setBeginOn(new java.sql.Date(c.getTimeInMillis))
        c.setTime(semester.getEndOn)
        c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + offset.intValue() * 7)
        semester.setEndOn(new java.sql.Date(c.getTimeInMillis))
        entityDao.saveOrUpdate(semester)
      }
    } catch {
      case e: Exception => return redirect("index", "info.save.failure")
    }
    redirect("index", "info.save.success")
  }

  def displayTeachers(): String = {
    val lessonId = getLongId("lesson")
    if (null == lessonId) {
      return forwardError("没有教学任务")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    put("departs", getDeparts)
    put("lesson", lesson)
    val activityWeek = CollectUtils.newHashMap()
    for (ca <- lesson.getCourseSchedule.getActivities) {
      activityWeek.put(ca.id, YearWeekTimeUtil.digest(ca.getTime.getWeekState, 2, 1, 52, getTextResource))
    }
    put("activityWeeks", activityWeek)
    put("weeks", WeekDays.All)
    forward()
  }

  def freeTeacherList(): String = {
    val lessonId = getLongId("lesson")
    if (null == lessonId) {
      return forwardError("没有教学任务")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val activityIds = CollectUtils.newHashSet(getAll("activity.id", classOf[Long]))
    val times = CollectUtils.newHashSet()
    for (activity <- lesson.getCourseSchedule.getActivities if activityIds.contains(activity.id)) times.add(activity.getTime)
    val teacher = populateEntity(classOf[Teacher], "searchTeacher")
    val units = Array.ofDim[CourseTime](times.size)
    times.toArray(units)
    var departments = CollectUtils.newArrayList()
    val allTeacher = getBoolean("allTeacher")
    if (true != allTeacher) {
      departments = getDeparts
    }
    put("teachers", teachResourceService.getFreeTeachersOf(lesson.getSemester, departments, units, teacher, 
      null, getPageLimit, get(Order.ORDER_STR)))
    forward()
  }

  def changeTeacher(): String = {
    val lessonId = getLongId("lesson")
    if (null == lessonId) {
      return forwardError("没有教学任务")
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    val activityIds = CollectUtils.newHashSet(getAll("activity.id", classOf[Long]))
    val toTeacherIdSeq = get("toTeacherId")
    if (Strings.isEmpty(toTeacherIdSeq)) {
      return forwardError("没有选择更换教师")
    }
    val newTeachers = entityDao.get(classOf[Teacher], Strings.splitToLong(toTeacherIdSeq))
    val activityList = CollectUtils.newArrayList(lesson.getCourseSchedule.getActivities)
    val fromweek = if ((null == getInt("fromweek"))) 0 else getInt("fromweek").intValue()
    val endweek = if ((null == getInt("endweek"))) 0 else getInt("endweek").intValue()
    val changeWeek = (fromweek <= endweek && fromweek >= 1 && endweek < 53)
    for (activity <- lesson.getCourseSchedule.getActivities) {
      if (!activityIds.contains(activity.id)) //continue
      var newactivity = activity
      if (changeWeek) {
        val time = activity.getTime
        if (BitStrings.binValueOf(Strings.substring(time.state, fromweek, endweek + 1)) > 
          0) {
          val oldState = new StringBuilder(time.state)
          val newState = new StringBuilder(Strings.repeat("0", oldState.length))
          var i = fromweek
          while (i <= endweek) {
            newState.setCharAt(i, oldState.charAt(i))
            oldState.setCharAt(i, '0')
            i += 1
          }
          if (oldState.indexOf("1") > -1) {
            newactivity = activity.clone().asInstanceOf[CourseActivity]
            newactivity.getTime.newWeekState(newState.toString)
            activityList.add(newactivity)
            activity.getTime.newWeekState(oldState.toString)
          }
        }
      }
      newactivity.getTeachers.clear()
      newactivity.getTeachers.addAll(newTeachers)
      if (newactivity.getTime.state == 0) activityList.remove(newactivity)
    }
    val mergedActivities = courseActivityService.mergeActivites(activityList)
    val messages = checkSchedule(lesson, mergedActivities)
    if (!messages.isEmpty) {
      return redirect("taskList", getText(messages.get(0).getKey, messages.get(0).getKey, messages.get(0).getParams), 
        "status=" + CourseStatusEnum.ARRANGED + "&lesson.semester.id=" + 
        lesson.getSemester.id)
    }
    lesson.getCourseSchedule.getActivities.clear()
    lesson.getCourseSchedule.getActivities.addAll(mergedActivities)
    val allTeachers = CollectUtils.newHashSet()
    for (ca <- mergedActivities) allTeachers.addAll(ca.getTeachers)
    lesson.getTeachers.retainAll(allTeachers)
    allTeachers.removeAll(lesson.getTeachers)
    lesson.getTeachers.addAll(allTeachers)
    try {
      entityDao.saveOrUpdate(lesson)
      scheduleLogHelper.log(ScheduleLogBuilder.update(lesson, "更换教师"))
      redirect("taskList", "info.save.success", "status=" + CourseStatusEnum.ARRANGED + "&lesson.semester.id=" + 
        lesson.getSemester.id)
    } catch {
      case e: Exception => redirect("taskList", "info.save.failure", "status=" + CourseStatusEnum.ARRANGED + "&lesson.semester.id=" + 
        lesson.getSemester.id)
    }
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
    } else if ("program" == kind) {
      collisions = courseActivityService.detectCollision(semester, ResourceType.PROGRAM, timeSetting)
    } else {
      return forward("unsurportedDetectType")
    }
    put("collisions", collisions)
    forward()
  }

  def modifyPeopleLimit(): String = {
    val taskIds = get("lesson.id")
    if (Strings.isEmpty(taskIds)) {
      return forwardError("error.teachTask.id.needed")
    }
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.project.id=:projectid1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    query.where("lesson.id in (:ids)", Strings.splitToLong(taskIds))
    var taskList = entityDao.search(query)
    taskList = DataAuthorityUtil.select("TeachTaskForTeachDepart", taskList, getStdTypeIdSeq, getDepartmentIdSeq)
    put("taskList", taskList)
    forward()
  }

  def saveModifyPeopleLimit(): String = {
    val lessons = entityDao.get(classOf[Lesson], Strings.splitToLong(get("taskIds")))
    for (lesson <- lessons) {
      lesson.getTeachClass.setLimitCount(getInt("limitCount" + lesson.id))
    }
    entityDao.saveOrUpdate(lessons)
    redirect("taskList", "info.save.success")
  }

  def calculateMaxClassCount(): String = {
    val taskId = getLong("lesson.id")
    if (null == taskId) {
      return forwardError("error.teachTask.id.needed")
    }
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.id = :taskId", taskId)
    query.where("lesson.teachClass.limitCount = 0")
    val lessons = entityDao.search(query)
    if (CollectUtils.isNotEmpty(lessons)) {
    }
    for (lesson <- lessons) {
      try {
      } catch {
        case e: Exception => return forwardError(lesson.getNo + "设置班级上限失败失败\n" + e.getStackTrace.toString)
      }
    }
    entityDao.saveOrUpdate(lessons)
    if (lessons.size == 0) {
      redirect("taskList", "")
    } else {
      redirect("taskList", "info.action.success")
    }
  }

  def roomUtilizations(): String = {
    putRoomUtilizationOfCourse(getDeparts, entityDao.get(classOf[Semester], getInt("semester.id")), getFloat("ratio"))
    put("textResource", getTextResource)
    put("courseActivityDigestor", CourseActivityDigestor.getInstance)
    forward()
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

  def checkCourseHours(): String = {
    val condition = get("condition")
    val query = getQueryBuilder
    query.limit(null)
    val lessonIds = getLongIds("lesson")
    if (ArrayUtils.isNotEmpty(lessonIds)) {
      query.where("lesson.id in (:lessonIds)", lessonIds)
    }
    val lessons = entityDao.search(query)
    val badTaskInfos = CollectUtils.newHashMap()
    for (lesson <- lessons) {
      val shouldOverallUnits = lesson.getCourse.getPeriod
      var actualOverallUnits = 0
      for (activity <- lesson.getCourseSchedule.getActivities) {
        val time = activity.getTime
        val weeks = Strings.count(time.state, "1")
        val units = time.getEndUnit - time.getStartUnit + 1
        actualOverallUnits += weeks * units
      }
      if (shouldOverallUnits != actualOverallUnits) {
        if (Objects.==(condition, "all")) {
          badTaskInfos.put(lesson, Array(shouldOverallUnits, actualOverallUnits))
        } else if (Objects.==(condition, "less") && actualOverallUnits < shouldOverallUnits) {
          badTaskInfos.put(lesson, Array(shouldOverallUnits, actualOverallUnits))
        } else if (Objects.==(condition, "more") && actualOverallUnits > shouldOverallUnits) {
          badTaskInfos.put(lesson, Array(shouldOverallUnits, actualOverallUnits))
        }
      }
    }
    put("badTaskInfos", badTaskInfos)
    forward()
  }

  def checkTeacherOfArrangedTask(): String = {
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.project.id=:projectid1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    query.where("lesson.semester.id = :semesterId", getInt("lesson.semester.id"))
    query.where("lesson.courseSchedule.status=:status", CourseStatusEnum.ARRANGED)
    query.where("exists (from lesson.teachers teacher)")
    val hql = new StringBuilder()
    hql.append("(select count(activity.id) from lesson.courseSchedule.activities activity where exists (from activity.teachers teacher where exists (from lesson.teachers lessonTeacher where lessonTeacher = teacher)))")
    hql.append(" != ")
    hql.append("(select count(activity.id) from lesson.courseSchedule.activities activity)")
    query.where(hql.toString)
    query.orderBy(Order.parse("lesson.no"))
    query.limit(null)
    val projectId = getInt("lesson.project.id")
    query.where("lesson.teachDepart in (:departments)", getDeparts)
    query.where("lesson.project.id = :projectId", projectId)
    val lessons = entityDao.search(query)
    val badTaskInfos = CollectUtils.newArrayList()
    for (i <- 0 until lessons.size) {
      val lesson = lessons.get(i)
      val teachers = new HashSet[Teacher]()
      for (activity <- lesson.getCourseSchedule.getActivities) {
        teachers.addAll(activity.getTeachers)
      }
      badTaskInfos.add(Array(lesson, teachers))
    }
    put("semesterId", getInt("lesson.semester.id"))
    put("badTaskInfos", badTaskInfos)
    forward()
  }

  def willAndBeenArrange(): String = {
    val semesterId = getInt("lesson.semester.id")
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.project.id=:projectid1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    query.where("lesson.semester.id = (:semesterId)", semesterId)
    val lessonsResults = entityDao.search(query)
    val lessons = new ArrayList[Lesson]()
    for (lesson <- lessonsResults) {
    }
    put("lessons", lessons)
    forward()
  }

  def setCourseActivityService(courseActivityService: CourseActivityService) {
    this.courseActivityService = courseActivityService
  }

  def setLessonService(lessonService: LessonService) {
    this.lessonService = lessonService
  }

  def setClassoomService(classroomService: RoomService) {
    this.classroomService = classroomService
  }

  def setLessonSearchHelper(lessonSearchHelper: LessonSearchHelper) {
    this.lessonSearchHelper = lessonSearchHelper
  }

  def setRoomService(classroomService: RoomService) {
    this.classroomService = classroomService
  }

  def setTimeSettingService(timeSettingService: TimeSettingService) {
    this.timeSettingService = timeSettingService
  }

  def getDepertmentTeacher(): String = {
    val departmentId = getInt("departmentId")
    if (null != departmentId) {
      val builder = OqlBuilder.from(classOf[Teacher], "teacher").where("teacher.department.id =:departmentId", 
        departmentId)
        .where("teacher.teaching is true")
        .where("teacher.effectiveAt <= :now and (teacher.invalidAt is null or teacher.invalidAt >= :now)", 
        new java.util.Date())
      put("teachers", entityDao.search(builder))
    } else {
      put("teachers", CollectionUtils.EMPTY_COLLECTION)
    }
    forward("departTeacher")
  }

  def setTeachResourceService(teachResourceService: TeachResourceService) {
    this.teachResourceService = teachResourceService
  }

  def setRoomResourceService(classroomResourceService: RoomResourceService) {
    this.classroomResourceService = classroomResourceService
  }

  def setScheduleLogHelper(scheduleLogHelper: ScheduleLogHelper) {
    this.scheduleLogHelper = scheduleLogHelper
  }

  def setCheckers(checkers: List[LessonScheduleChecker]) {
    this.checkers = checkers
  }

  def setTeacherPeriodLimitService(teacherPeriodLimitService: TeacherPeriodLimitService) {
    this.teacherPeriodLimitService = teacherPeriodLimitService
  }

  def getCourseLimitService(): CourseLimitService = courseLimitService

  def setCourseLimitService(courseLimitService: CourseLimitService) {
    this.courseLimitService = courseLimitService
  }

  def setScheduleRoomService(scheduleRoomService: ScheduleRoomService) {
    this.scheduleRoomService = scheduleRoomService
  }
}
