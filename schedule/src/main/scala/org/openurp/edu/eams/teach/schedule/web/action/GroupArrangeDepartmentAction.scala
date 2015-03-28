package org.openurp.edu.eams.teach.schedule.web.action


import java.util.Arrays
import java.util.Comparator





import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.Operation
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.beangle.struts2.convention.route.Action
import org.openurp.edu.eams.base.Building
import org.openurp.base.Campus
import org.openurp.base.Room
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
import org.openurp.edu.base.Project
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.teach.code.industry.TeachLangType
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.model.CourseActivityBean
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.service.CourseTableStyle
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.eams.teach.schedule.log.ScheduleLogBuilder
import org.openurp.edu.eams.teach.schedule.model.CollisionInfo
import org.openurp.edu.eams.teach.schedule.model.CollisionResource.ResourceType
import org.openurp.edu.eams.teach.schedule.service.CourseActivityService
import org.openurp.edu.eams.teach.schedule.service.ScheduleLogHelper
import org.openurp.edu.eams.teach.schedule.service.ScheduleRoomService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class GroupArrangeDepartmentAction extends SemesterSupportAction {

  var lessonService: LessonService = _

  var timeSettingService: TimeSettingService = _

  var lessonSearchHelper: LessonSearchHelper = _

  var classroomResourceService: RoomResourceService = _

  var scheduleLogHelper: ScheduleLogHelper = _

  var courseActivityService: CourseActivityService = _

  var scheduleRoomService: ScheduleRoomService = _

  override def indexSetting() {
    val semester = putSemester(null)
    val project = getProject
    put("courseTypes", lessonService.courseTypesOfSemester(Collections.newBuffer[Any](project), getDeparts, 
      semester))
    put("teachDepartList", lessonService.teachDepartsOfSemester(Collections.newBuffer[Any](project), getDeparts, 
      semester))
    put("departmentList", getCollegeOfDeparts)
    put("stdTypeList", getStdTypes)
    addBaseCode("languages", classOf[TeachLangType])
    put("weeks", WeekDays.All)
    val setting = timeSettingService.getClosestTimeSetting(project, semester, null)
    put("units", if (setting == null) 0 else setting.getDefaultUnits.size)
    put("courseStatusEnums", CourseStatusEnum.values)
    val status = get("status")
    if (Strings.isEmpty(status)) {
      put("currentStatus", CourseStatusEnum.NEED_ARRANGE)
    } else {
      put("currentStatus", CourseStatusEnum.valueOf(status))
    }
  }

  def taskList(): String = {
    val query = lessonSearchHelper.buildQuery()
    query.where("lesson.project.id = :projectId1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    if (Strings.isEmpty(get(Order.ORDER_STR))) {
      query.orderBy("lesson.no")
    }
    val isArrangeCompleted = get("status")
    put("teacherIsNull", getBool("fake.teacher.null"))
    if (Strings.isNotEmpty(isArrangeCompleted)) {
      if (isArrangeCompleted == CourseStatusEnum.NEED_ARRANGE.toString) {
        query.where("size(lesson.schedule.activities) = 0")
        query.where("lesson.schedule.status = :status", CourseStatusEnum.NEED_ARRANGE)
        put("courseStatusEnum", CourseStatusEnum.NEED_ARRANGE)
      } else if (isArrangeCompleted == CourseStatusEnum.DONT_ARRANGE.toString) {
        query.where("lesson.schedule.status = :status", CourseStatusEnum.DONT_ARRANGE)
        put("courseStatusEnum", CourseStatusEnum.DONT_ARRANGE)
      } else if (isArrangeCompleted == CourseStatusEnum.ARRANGED.toString) {
        query.where("lesson.schedule.status = :status", CourseStatusEnum.ARRANGED)
        put("courseStatusEnum", CourseStatusEnum.ARRANGED)
      }
    }
    put("project", getProject)
    put("semester", putSemester(null))
    val lessons = entityDao.search(query)
    put("lessons", lessons)
    val digestor = CourseActivityDigestor.getInstance.setDelimeter("<br>")
    val arrangeInfo = Collections.newMap[Any]
    for (oneTask <- lessons) {
      arrangeInfo.put(oneTask.id.toString, digestor.digest(getTextResource, oneTask))
    }
    put("arrangeInfo", arrangeInfo)
    put("weekStates", new WeekStates())
    forward()
  }

  def groupArrange(): String = {
    val lessons = getModels(classOf[Lesson], getLongIds("lesson"))
    if (lessons.isEmpty) {
      return forwardError("error.model.ids.needed")
    }
    put("lessons", lessons)
    put("weekList", WeekDays.All)
    val setting = timeSettingService.getClosestTimeSetting(getProject, putSemester(null), null)
    put("timeSetting", setting)
    put("tableStyle", CourseTableStyle.getStyle(getConfig.get(CourseTableStyle.STYLE_KEY).asInstanceOf[String]))
    put("units", if (setting == null) 0 else setting.getDefaultUnits.size)
    put("CONTINUELY", CourseTime.CONTINUELY)
    put("EVEN", CourseTime.EVEN)
    put("ODD", CourseTime.ODD)
    forward()
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

  def freeRoomList(): String = {
    val lessonId = getLong("lessonId")
    val confilctRoom = getBool("confilctRoom")
    put("configTypeList", baseCodeService.getCodes(classOf[RoomType]))
    put("confilctRoom", confilctRoom)
    if (null == lessonId) {
      put("classroomList", Collections.emptyList())
      return forward()
    }
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    if (null == lesson) {
      put("classroomList", Collections.emptyList())
      return forward()
    }
    var room: Room = null
    if (getBool("default")) {
      room = Model.newInstance(classOf[Room])
      room.setCapacity(lesson.getTeachClass.getLimitCount)
      room.setType(lesson.getCourseSchedule.getRoomType)
    } else {
      room = populate(classOf[Room], "classroom")
    }
    var selectedWeekUnitSeq = get("weekUnits")
    selectedWeekUnitSeq = selectedWeekUnitSeq.replaceAll("<br>", "")
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
      val newTime = YearWeekTimeUtil.buildYearWeekTimes(2, lesson.getCourseSchedule.getStartWeek, lesson.getCourseSchedule.getEndWeek, 
        CourseTime.CONTINUELY)
      newTime.setWeekday(weekId)
      newTime.setStartUnit(unitId)
      newTime.setEndUnit(unitId)
      timeList.add(newTime)
    }
    val project = getProject
    val semester = lesson.getSemester
    val timeSetting = timeSettingService.getClosestTimeSetting(project, semester, null)
    for (j <- 0 until timeList.size) {
      val unit = timeList.get(j)
      unit.setStartTime(timeSetting.getDefaultUnits.get(unit.getStartUnit).start)
      unit.setEndTime(timeSetting.getDefaultUnits.get(unit.getEndUnit).end)
    }
    val activity = Model.newInstance(classOf[CourseActivity])
    activity.setRooms(new HashSet[Room]())
    activity.getRooms.add(room)
    activity.setLesson(lesson)
    var builder: OqlBuilder[Room] = null
    builder = if (confilctRoom) scheduleRoomService.getOccupancyRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
      activity)
      .orderBy("classroom.capacity,classroom.code")
      .limit(getPageLimit) else scheduleRoomService.getFreeRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
      activity)
      .orderBy("classroom.capacity,classroom.code")
      .limit(getPageLimit)
    if (room.getCampus != null) {
      put("buildingList", CollectionUtils.intersection(baseInfoService.getBaseInfos(classOf[Building]), 
        entityDao.get(classOf[Building], "campus", room.getCampus)))
    } else {
      put("buildingList", Collections.EMPTY_LIST)
    }
    put("campusList", baseInfoService.getBaseInfos(classOf[Campus]))
    put("classroomList", entityDao.search(builder))
    put("lesson", lesson)
    forward()
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
        scheduleLogHelper.log(ScheduleLogBuilder.delete(entityDao.get(classOf[Lesson], lessonId), "批量排课"))
      }
    } catch {
      case e: Exception => return redirect("taskList", "info.delete.failure", "status=" + CourseStatusEnum.ARRANGED + "&lesson.semester.id=" + 
        semesterId)
    }
    redirect("taskList", "info.delete.success", "status=" + CourseStatusEnum.ARRANGED + "&lesson.semester.id=" + 
      semesterId)
  }

  def batchSaveActivities(): String = {
    val lessons = getModels(classOf[Lesson], getLongIds("lesson"))
    if (lessons.isEmpty) {
      return forwardError("error.model.ids.needed")
    }
    val count = getInt("activityCount")
    if (null == count || count == 0) {
      return forwardError("没有安排小节")
    }
    val failureArrangeMap = Collections.newMap[Any]
    val successArrangeMap = Collections.newMap[Any]
    for (lesson <- lessons) {
      if (CourseStatusEnum.NEED_ARRANGE != lesson.getCourseSchedule.getStatus || 
        !lesson.getCourseSchedule.getActivities.isEmpty) {
        failureArrangeMap.put(lesson, "该任务已有排课活动或已排课")
        //continue
      }
      if (lesson.getTeachers.size > 1) {
        failureArrangeMap.put(lesson, "该任务有多个教师。请为其单独排课")
        //continue
      }
      val activityList = Collections.newBuffer[Any](count.intValue())
      for (i <- 0 until count.intValue()) {
        val activity = populate(classOf[CourseActivity], "activity" + i)
        val cycle = getInt("activity" + i + ".cycle")
        activity.setTeachers(Collections.newHashSet(lesson.getTeachers))
        val time = YearWeekTimeUtil.buildYearWeekTimes(2, lesson.getCourseSchedule.getStartWeek, lesson.getCourseSchedule.getEndWeek, 
          cycle)
        activity.getTime.newWeekState(time.state)
        activityList.add(activity)
      }
      val mergedActivityList = CourseActivityBean.mergeActivites(activityList)
      if (getBool("stdConfict")) {
        val collisionTakes = courseActivityService.collisionTakes(lesson, mergedActivityList)
        if (Collections.isNotEmpty(collisionTakes)) {
          failureArrangeMap.put(lesson, "该任务已有学生选课,并且排课造成学生冲突")
          //continue
        }
      }
      var period = 0
      val timeList = Collections.newBuffer[Any]
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
        timeList.add(time)
      }
      var classrooms = Collections.newBuffer[Any]
      val suggestRoom = getBool("suggestRoom")
      if (suggestRoom) {
        val roomForSearch = Model.newInstance(classOf[Room])
        roomForSearch.setCapacity(lesson.getTeachClass.getLimitCount)
        roomForSearch.setType(lesson.getCourseSchedule.getRoomType)
        val activityForSearch = Model.newInstance(classOf[CourseActivity])
        activityForSearch.getRooms.add(roomForSearch)
        activityForSearch.setLesson(lesson)
        classrooms = entityDao.search(scheduleRoomService.getFreeRoomsOf(getDeparts, timeList.toArray(Array.ofDim[CourseTime](timeList.size)), 
          activityForSearch)
          .orderBy("classroom.capacity,classroom.code")
          .limit(getPageLimit))
      } else {
        val roomIds = get("lesson" + lesson.id + ".classroom.id")
        if (Strings.isEmpty(roomIds)) {
          failureArrangeMap.put(lesson, "没有设置教室")
          //continue
        }
        classrooms = entityDao.get(classOf[Room], Strings.splitToInt(roomIds))
      }
      if (classrooms.isEmpty) {
        failureArrangeMap.put(lesson, "没有找到合适的空闲教室")
        //continue
      }
      var collision = false
      val occupancies = Collections.newSet[Any]
      for (activity <- mergedActivityList) {
        if (suggestRoom) {
          activity.getRooms.add(classrooms.get(0))
        } else {
          activity.getRooms.addAll(classrooms)
        }
        if (getBool("teacherConfict")) {
          if (courseActivityService.isCourseActivityTeacherOccupied(activity)) {
            failureArrangeMap.put(lesson, "存在教师冲突")
            collision = true
            //break
          }
        }
        if (getBool("roomConfict")) {
          if (courseActivityService.isCourseActivityRoomOccupied(activity)) {
            failureArrangeMap.put(lesson, "存在教室冲突")
            collision = true
            //break
          }
        }
        val rooms = activity.getRooms
        val courseTime = activity.getTime
        val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(lesson, courseTime)
        for (room <- rooms; timeUnit <- timeUnits) {
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
      if (collision) {
        //continue
      }
      lesson.getCourseSchedule.setStatus(CourseStatusEnum.ARRANGED)
      lesson.getCourseSchedule.getActivities.addAll(mergedActivityList)
      lesson.getCourseSchedule.setPeriod(period)
      try {
        entityDao.execute(Operation.saveOrUpdate(lesson).saveOrUpdate(occupancies))
        scheduleLogHelper.log(ScheduleLogBuilder.create(lesson, "批量排课"))
      } catch {
        case e: Exception => {
          failureArrangeMap.put(lesson, e.getMessage)
          //continue
        }
      }
      val digestor = CourseActivityDigestor.getInstance.setDelimeter("<br>")
      successArrangeMap.put(lesson, digestor.digest(getTextResource, lesson, ":day :units :weeks :room :roomCode"))
    }
    put("failureArrangeMap", failureArrangeMap)
    put("successArrangeMap", successArrangeMap)
    put("semester", putSemester(null))
    if (failureArrangeMap.size > 0 && successArrangeMap.size > 0) {
      var lessonIds = ""
      val lessonIterator = successArrangeMap.keySet.iterator()
      while (lessonIterator.hasNext) {
        lessonIds += lessonIterator.next().id
        if (lessonIterator.hasNext) {
          lessonIds += ","
        }
      }
      put("toRemoveLessonIds", lessonIds)
    }
    forward("arrangeResults")
  }
}
