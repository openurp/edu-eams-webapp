package org.openurp.edu.eams.teach.schedule.web.action


import java.util.Arraysimport java.util.Comparator
import java.util.GregorianCalendar




import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Transformer
import org.apache.poi.ss.usermodel.DateUtil
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.openurp.base.Room
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.base.TimeSetting
import org.openurp.edu.eams.base.code.school.RoomType
import org.openurp.edu.eams.classroom.Occupancy
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.base.code.RoomUsage
import org.openurp.edu.eams.classroom.model.OccupancyBean
import org.openurp.edu.eams.classroom.service.RoomResourceService
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator.Usage
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.RoomService
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseActivityBean
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.eams.teach.schedule.log.ScheduleLogBuilder
import org.openurp.edu.eams.teach.schedule.model.CourseArrangeAlteration
import org.openurp.edu.eams.teach.schedule.service.CourseActivityService
import org.openurp.edu.eams.teach.schedule.service.ScheduleLogHelper
import org.openurp.edu.eams.teach.schedule.service.ScheduleRoomService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class ReplaceRoomAction extends SemesterSupportAction {

  protected var classroomService: RoomService = _

  protected var classroomResourceService: RoomResourceService = _

  protected var timeSettingService: TimeSettingService = _

  protected var courseActivityService: CourseActivityService = _

  protected var scheduleLogHelper: ScheduleLogHelper = _

  protected var scheduleRoomService: ScheduleRoomService = _

  def index(): String = {
    setSemesterDataRealm(hasStdType)
    val semester = getAttribute("semester").asInstanceOf[Semester]
    put("maxunits", timeSettingService.getClosestTimeSetting(getProject, semester, null)
      .getDefaultUnits
      .size)
    forward()
  }

  def search(): String = {
    val semesterId = getInt("semester.id")
    val semester = entityDao.get(classOf[Semester], semesterId)
    val query = OqlBuilder.from(classOf[CourseActivity], "courseActivity")
    if (semesterId != null) {
      query.where("courseActivity.lesson.semester.id = :semesterId", semesterId)
    }
    populateConditions(query)
    val teacherName = get("teacherName")
    if (Strings.isNotEmpty(teacherName)) {
      query.join("courseActivity.teachers", "teacher")
      query.where(Condition.like("teacher.name", teacherName))
    }
    val roomName = get("roomName")
    if (Strings.isNotEmpty(roomName)) {
      query.join("courseActivity.rooms", "room")
      query.where(Condition.like("room.name", roomName))
    }
    query.limit(getPageLimit)
    query.orderBy(Order.parse(get("orderBy")))
    val courseActivities = entityDao.search(query)
    val weekTimeMap = CollectUtils.newHashMap()
    for (courseActivity <- courseActivities) {
      val ct = courseActivity.getTime
      val weekTimeStr = YearWeekTimeUtil.digest(ct.getWeekState, 2, 1, semester.getWeeks, null)
      weekTimeMap.put(courseActivity.id.toString, weekTimeStr)
    }
    put("courseActivities", entityDao.search(query))
    put("weekTimeMap", weekTimeMap)
    forward()
  }

  def freeRoomListBatch(): String = {
    val activityId = getLong("activityId")
    val confilctRoom = getBool("confilctRoom")
    put("configTypeList", baseCodeService.getCodes(classOf[RoomType]))
    put("confilctRoom", confilctRoom)
    if (null == activityId) {
      put("classroomList", Collections.emptyList())
      return forward()
    }
    val activity = entityDao.get(classOf[CourseActivity], activityId)
    if (null == activity) {
      put("classroomList", Collections.emptyList())
      return forward()
    }
    var room: Room = null
    if (getBool("default")) {
      room = Model.newInstance(classOf[Room])
      room.setCapacity(activity.getLesson.getTeachClass.getLimitCount)
      room.setType(activity.getLesson.getCourseSchedule.getRoomType)
    } else {
      room = populate(classOf[Room], "classroom")
    }
    val timeList = new ArrayList[CourseTime]()
    timeList.add(activity.getTime)
    val targetActivity = Model.newInstance(classOf[CourseActivity])
    targetActivity.getRooms.add(room)
    targetActivity.setLesson(activity.getLesson)
    var builder: OqlBuilder[Room] = null
    builder = if (confilctRoom) scheduleRoomService.getOccupancyRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
      targetActivity)
      .orderBy("classroom.capacity,classroom.code")
      .limit(getPageLimit) else scheduleRoomService.getFreeRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
      targetActivity)
      .orderBy("classroom.capacity,classroom.code")
      .limit(getPageLimit)
    put("classroomList", entityDao.search(builder))
    put("activity", activity)
    forward()
  }

  def batchReplaceRoomSave(): String = {
    val activities = getModels(classOf[CourseActivity], getLongIds("courseActivity"))
    val successes = CollectUtils.newHashMap()
    val failures = CollectUtils.newHashMap()
    val digestor = CourseActivityDigestor.getInstance
    for (courseActivity <- activities) {
      val lesson = courseActivity.getLesson
      val alterationBefore = digestor.digest(null, lesson)
      val rooms = getModels(classOf[Room], Strings.splitToLong(get("courseActivity" + courseActivity.id + ".classroom.id")))
      var changeInfo = "变更前:"
      for (classroom <- courseActivity.getRooms) {
        changeInfo += classroom.getName + ";"
      }
      val builder = OqlBuilder.from(classOf[Occupancy], "occupancy")
      builder.where("occupancy.room in (:rooms)", courseActivity.getRooms)
      builder.where("occupancy.usage.id = :usageId", RoomUsage.COURSE)
      builder.where("occupancy.userid = :userid", RoomUseridGenerator.gen(courseActivity.getLesson, Usage.COURSE))
      val occupancies = entityDao.search(builder)
      changeInfo += "变更后:"
      courseActivity.getRooms.clear()
      val toSaveOccupancies = CollectUtils.newHashSet()
      if (!rooms.isEmpty) {
        courseActivity.getRooms.addAll(rooms)
        val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(lesson, courseActivity.getTime)
        for (classroom2 <- rooms) {
          changeInfo += classroom2.getName + ";"
          for (timeUnit <- timeUnits) {
            val occupancy = new OccupancyBean()
            occupancy.setRoom(classroom2)
            occupancy.setUserid(RoomUseridGenerator.gen(lesson, Usage.COURSE))
            occupancy.setUsage(entityDao.get(classOf[RoomUsage], RoomUsage.COURSE))
            occupancy.setTime(timeUnit)
            occupancy.setComments(lesson.getNo + "[" + lesson.getCourse.getName + "]")
            toSaveOccupancies.add(occupancy)
          }
        }
      } else {
        changeInfo += "无"
      }
      if (courseActivityService.isCourseActivityRoomOccupied(courseActivity)) {
        failures.put(courseActivity, "存在教室冲突")
        //continue
      }
      try {
        entityDao.execute(Operation.remove(occupancies).saveOrUpdate(lesson).saveOrUpdate(toSaveOccupancies))
        if (Strings.isNotEmpty(alterationBefore) && 
          CourseStatusEnum.ARRANGED == lesson.getCourseSchedule.getStatus) {
          entityDao.refresh(lesson)
          val alteration = new CourseArrangeAlteration()
          alteration.setLessonId(lesson.id)
          alteration.setSemester(lesson.getSemester)
          alteration.setAlterationBefore(alterationBefore)
          val alterationAfter = digestor.digest(null, lesson)
          if (Objects.!=(alterationBefore, alterationAfter)) {
            alteration.setAlterationAfter(alterationAfter)
            alteration.setAlterBy(entityDao.get(classOf[User], getUserId))
            alteration.setAlterFrom(getRemoteAddr)
            alteration.setAlterationAt(new java.util.Date())
            entityDao.saveOrUpdate(alteration)
          }
        }
        scheduleLogHelper.log(ScheduleLogBuilder.update(lesson, "批量更换教室"))
        successes.put(courseActivity, changeInfo)
      } catch {
        case e: Exception => failures.put(courseActivity, e.getMessage)
      }
    }
    put("successes", successes)
    put("failures", failures)
    forward("batchUpdateResult")
  }

  def batchReplaceRoom(): String = {
    val courseActivities = getModels(classOf[CourseActivity], getLongIds("courseActivity"))
    val weekTimeMap = CollectUtils.newHashMap()
    for (courseActivity <- courseActivities) {
      weekTimeMap.put(courseActivity, YearWeekTimeUtil.digest(courseActivity.getTime.getWeekState, 2, 1, 
        courseActivity.getLesson.getSemester.getWeeks, null))
    }
    put("weekTimeMap", weekTimeMap)
    put("courseActivities", courseActivities)
    forward()
  }

  def replaceRoom(): String = {
    val courseActivityId = getLongId("courseActivity")
    val courseActivity = entityDao.get(classOf[CourseActivity], courseActivityId)
    put("lesson", courseActivity.getLesson)
    val semester = courseActivity.getLesson.getSemester
    val query = OqlBuilder.from(classOf[CourseActivity], "courseActivity")
    query.where("courseActivity.id = :courseActivityId", courseActivityId)
    populateConditions(query)
    query.orderBy(Order.parse(get("orderBy")))
    put("courseActivities", entityDao.search(query))
    put("ca", courseActivity)
    val weekTimeMap = CollectUtils.newHashMap()
    val ct = courseActivity.getTime
    val weekTimeStr = YearWeekTimeUtil.digest(ct.getWeekState, 2, 1, semester.getWeeks, null)
    weekTimeMap.put(courseActivity.id.toString, weekTimeStr)
    put("weekTimeMap", weekTimeMap)
    put("weekStateArray", courseActivity.getTime.getWeekState.toCharArray())
    forward()
  }

  def replaceRoomSave(): String = {
    val courseActivityId = getLongId("courseActivity")
    val courseActivity = entityDao.get(classOf[CourseActivity], courseActivityId)
    val lesson = courseActivity.getLesson
    val lessonId = lesson.id
    val activityList = CollectUtils.newArrayList()
    val roomIds = Strings.splitToInt(get("roomIds"))
    val roomList = entityDao.get(classOf[Room], roomIds)
    val weekStates = get("weekStates")
    val courseTimeOld = courseActivity.getTime
    val oldWeekState = courseTimeOld.getWeekState
    val oldWeekStateChar = oldWeekState.toCharArray()
    val weekStateChangeChar = weekStates.toCharArray()
    val newWeekStateChar = Array.ofDim[Char](oldWeekStateChar.length)
    var nn = 0
    for (i <- 0 until oldWeekStateChar.length) {
      val week = oldWeekStateChar(i)
      newWeekStateChar(i) = oldWeekStateChar(i)
      if (week == '1') {
        if (weekStateChangeChar(nn) == '1') {
          oldWeekStateChar(i) = '0'
          newWeekStateChar(i) = '1'
        } else {
          newWeekStateChar(i) = '0'
        }
        nn += 1
      }
    }
    courseActivity.getTime.newWeekState(String.valueOf(oldWeekStateChar))
    val activityNew = this.createNewCourseActivity(courseActivity, roomList, weekStates)
    activityNew.getTime.newWeekState(String.valueOf(newWeekStateChar))
    activityList.add(activityNew)
    var occupancies = CollectUtils.newArrayList()
    if (!courseActivity.getRooms.isEmpty) {
      val builder = OqlBuilder.from(classOf[Occupancy], "occupancy")
      builder.where("occupancy.room in (:rooms)", courseActivity.getRooms)
      builder.where("occupancy.usage.id = :usageId", RoomUsage.COURSE)
      builder.where("occupancy.userid = :userid", RoomUseridGenerator.gen(courseActivity.getLesson, Usage.COURSE))
      occupancies = entityDao.search(builder)
    }
    val toRemoveActivities = CollectUtils.newArrayList()
    if (!courseActivity.getTime.getWeekState.contains("1")) {
      toRemoveActivities.add(courseActivity)
    } else {
      activityList.add(courseActivity)
    }
    val mergedActivityList = CourseActivityBean.mergeActivites(activityList)
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
      val toSaveOccupancies = CollectUtils.newHashSet()
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
        val coursePeriod = (time.getEndUnit - time.getStartUnit + 1) * lesson.getCourseSchedule.getWeeks
        period += coursePeriod
        if (courseActivityService.isCourseActivityRoomOccupied(activity)) {
          val errMsg = new StringBuilder()
          errMsg.append(Strings.join(CollectionUtils.collect(activity.getRooms, roomNameCollector), ","))
            .append(" 周")
            .append(activity.getTime.day)
            .append(" 第")
            .append(activity.getTime.getStartUnit)
            .append("小节-第")
            .append(activity.getTime.getEndUnit)
            .append("小节")
          return forwardError(Array(getText("lesson.courseSchedule.roomIsOccupied"), errMsg.toString))
        }
        val rooms = activity.getRooms
        val courseTime = activity.getTime
        val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(lesson, courseTime)
        val freeRooms = entityDao.search(scheduleRoomService.getFreeRoomsOfConditions(timeUnits))
        for (room <- rooms) {
          if (!freeRooms.contains(room) && !lessonOccupancyRooms.contains(room)) {
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
            toSaveOccupancies.add(occupancy)
          }
        }
      }
      lesson.getCourseSchedule.getActivities.addAll(mergedActivityList)
      lesson.getCourseSchedule.getActivities.removeAll(toRemoveActivities)
      entityDao.execute(Operation.remove(occupancies).saveOrUpdate(lesson).saveOrUpdate(toSaveOccupancies))
      scheduleLogHelper.log(ScheduleLogBuilder.update(lesson, "更换教室"))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        logHelper.info("Failure in deleting activities of lesson with id:" + 
          lessonId, e)
        return forwardError("error.occurred")
      }
    }
    redirect("search", "info.action.success")
  }

  def createNewCourseActivity(courseActivity: CourseActivity, roomList: List[Room], weekStates: String): CourseActivity = {
    val activityNew = courseActivity.clone().asInstanceOf[CourseActivity]
    activityNew.getRooms.clear()
    activityNew.getRooms.addAll(roomList)
    activityNew
  }

  def freeRoomList(): String = {
    val courseActivityId = getLong("courseActivityId")
    val courseActivity = entityDao.get(classOf[CourseActivity], courseActivityId)
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
      var selectedWeekUnitSeq = ""
      val courseTime = courseActivity.getTime
      if (courseTime.getStartUnit == courseTime.getEndUnit) {
        selectedWeekUnitSeq = courseTime.day + "," + courseTime.getStartUnit + 
          ";"
      } else {
        var i = courseTime.getStartUnit
        while (i <= courseTime.getEndUnit) {
          selectedWeekUnitSeq += courseTime.day + "," + i + ";"
          i += 1
        }
      }
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
      val project: Project = null
      val semester = semesterService.getSemester(semesterIdForFreeRoom)
      val timeSetting = timeSettingService.getClosestTimeSetting(project, semester, null)
      for (j <- 0 until timeList.size) {
        val unit = timeList.get(j)
        unit.setStartTime(timeSetting.getDefaultUnits.get(unit.getStartUnit).start)
        unit.setEndTime(timeSetting.getDefaultUnits.get(unit.getEndUnit).end)
      }
      val activity = Model.newInstance(classOf[CourseActivity])
      activity.setRooms(new HashSet[Room]())
      activity.getRooms.add(room)
      activity.setLesson(courseActivity.getLesson)
      val builder = scheduleRoomService.getFreeRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
        activity)
        .orderBy("classroom.capacity,classroom.code")
        .limit(getPageLimit)
      put("classroomList", entityDao.search(builder))
    }
    put("configTypeList", baseCodeService.getCodes(classOf[RoomType]))
    put("detectCollision", detectCollision)
    forward()
  }

  def remove(): String = {
    val courseActivityIds = getLongIds("courseActivity")
    val courseActivityList = entityDao.get(classOf[CourseActivity], courseActivityIds)
    try {
      entityDao.remove(courseActivityList)
    } catch {
      case e: Exception => return redirect("search", "info.delete.failure")
    }
    redirect("search", "info.action.success")
  }

  def transformWeekState(selectedWeeks: String, oldWeekState: String): String = {
    val oldWeekStateChar = oldWeekState.toCharArray()
    val weekStateChangeChar = selectedWeeks.toCharArray()
    var nn = 0
    for (i <- 0 until oldWeekStateChar.length) {
      val week = oldWeekStateChar(i)
      if (week == '1') {
        oldWeekStateChar(i) = weekStateChangeChar(nn)
        nn += 1
      }
    }
    String.valueOf(oldWeekStateChar)
  }

  def getRoomService(): RoomService = classroomService

  def setRoomService(classroomService: RoomService) {
    this.classroomService = classroomService
  }

  def getRoomResourceService(): RoomResourceService = classroomResourceService

  def setRoomResourceService(classroomResourceService: RoomResourceService) {
    this.classroomResourceService = classroomResourceService
  }

  def getTimeSettingService(): TimeSettingService = timeSettingService

  def setTimeSettingService(timeSettingService: TimeSettingService) {
    this.timeSettingService = timeSettingService
  }

  def getCourseActivityService(): CourseActivityService = courseActivityService

  def setCourseActivityService(courseActivityService: CourseActivityService) {
    this.courseActivityService = courseActivityService
  }

  def setScheduleLogHelper(scheduleLogHelper: ScheduleLogHelper) {
    this.scheduleLogHelper = scheduleLogHelper
  }

  def setScheduleRoomService(scheduleRoomService: ScheduleRoomService) {
    this.scheduleRoomService = scheduleRoomService
  }
}
