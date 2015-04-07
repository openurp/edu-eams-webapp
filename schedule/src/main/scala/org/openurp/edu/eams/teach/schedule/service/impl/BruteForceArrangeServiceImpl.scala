package org.openurp.edu.eams.teach.schedule.service.impl

import java.util.Comparator


import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.openurp.base.Room
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.base.code.RoomUsage
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator.Usage
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.eams.teach.schedule.log.ScheduleLogBuilder
import org.openurp.edu.eams.teach.schedule.service.BruteForceArrangeContext
import org.openurp.edu.eams.teach.schedule.service.BruteForceArrangeContext.CommonConflictInfo
import org.openurp.edu.eams.teach.schedule.service.BruteForceArrangeService
import org.openurp.edu.eams.teach.schedule.service.CourseActivityService
import org.openurp.edu.eams.teach.schedule.service.ScheduleLogHelper
import org.openurp.edu.eams.teach.schedule.service.ScheduleRoomService



class BruteForceArrangeServiceImpl extends BaseServiceImpl with BruteForceArrangeService {

  var courseActivityService: CourseActivityService = _

  var scheduleRoomService: ScheduleRoomService = _

  var scheduleLogHelper: ScheduleLogHelper = _

  def bruteForceArrange(context: BruteForceArrangeContext, rooms: Iterable[Room]) {
    val lesson = context.lesson
    val filteredRooms = Collections.newBuffer[Any](CollectionUtils.select(rooms, new Predicate() {

      def evaluate(`object`: AnyRef): Boolean = {
        var room = `object`.asInstanceOf[Room]
        return room.campus == lesson.campus && 
          room.capacity >= lesson.teachClass.limitCount
      }
    }))
    Collections.sort(filteredRooms, new Comparator[Room]() {

      def compare(o1: Room, o2: Room): Int = return o1.capacity - o2.capacity
    })
    if (Collections.isEmpty(filteredRooms)) {
      context.noSuitableRoom()
      return
    }
    val activities = context.getTransientActivities
    detectUnresolvableConflict(context)
    if (context.hasUnResolvableConflict()) {
      context.failed()
      return
    }
    nextRoom: for (room <- filteredRooms) {
      for (activity <- activities) {
        activity.getRooms.clear()
        activity.getRooms.add(room)
        val roomsConflictInfo = detectRoomConflict(context, activity)
        if (roomsConflictInfo.hasCollictInfo()) {
          //continue
        }
      }
      saveActivities(context, activities)
      return
    }
    nextActivity: for (activity <- activities) {
      for (room <- filteredRooms) {
        activity.getRooms.clear()
        activity.getRooms.add(room)
        val roomsConflictInfo = detectRoomConflict(context, activity)
        if (!roomsConflictInfo.hasCollictInfo()) {
          //continue
        }
      }
      context.failed()
      return
    }
    saveActivities(context, activities)
  }

  private def saveActivities(context: BruteForceArrangeContext, activities: Iterable[CourseActivity]) {
    val lesson = context.lesson
    val isUpdateOperation = lesson.schedule.activities.size > 1
    val alterationBefore = CourseActivityDigestor.getInstance.digest(null, lesson)
    val occupancies = Collections.newSet[Any]
    var period = 0
    for (activity <- activities) {
      period += (activity.time.endUnit - activity.getTime.getStartUnit + 
        1) * 
        Strings.count(activity.time.weekState, "1")
      val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(lesson, activity.getTime)
      for (timeUnit <- timeUnits) {
        val occupancy = new OccupancyBean()
        for (room <- activity.getRooms) {
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
    lesson.schedule.activities.clear()
    courseActivityService.removeActivities(Array(lesson.id), lesson.semester)
    lesson.schedule.activities ++= activities
    lesson.schedule.period += period
    courseActivityService.saveOrUpdateActivity(lesson, occupancies, alterationBefore, false, entityDao.get(classOf[User], 
      SecurityUtils.getUserId), getRemoteAddr)
    scheduleLogHelper.log(if (isUpdateOperation) ScheduleLogBuilder.update(lesson, "手工排课") else ScheduleLogBuilder.create(lesson, 
      "手工排课"))
    context.succeed()
  }

  private def getRemoteAddr(): String = {
    val request = ServletActionContext.getRequest
    if (null == request) return null
    RequestUtils.getIpAddr(request)
  }

  private def detectUnresolvableConflict(context: BruteForceArrangeContext) {
    if (context.detectTake) {
      val collisionTakes = courseActivityService.collisionTakes(context.lesson, context.getTransientActivities)
      context.takeConflictInfo.addConflictInfo(collisionTakes, "与本任务上课时间冲突")
    }
    if (context.detectTeacher) {
      for (activity <- context.getTransientActivities if courseActivityService.isCourseActivityTeacherOccupied(activity)) {
        val errMsg = new StringBuilder()
        errMsg.append(" 周").append(activity.time.day)
          .append(" 第")
          .append(activity.time.startUnit)
          .append("小节-第")
          .append(activity.time.endUnit)
          .append("小节")
        context.teacherConflictInfo.addConflictInfo(activity.teachers, errMsg.toString)
      }
    }
  }

  private def detectRoomConflict(context: BruteForceArrangeContext, activity: CourseActivity): CommonConflictInfo[Room] = {
    val roomsConflictInfo = context.buildRoomsConflictInfo()
    if (context.detectRoom) {
      if (courseActivityService.isCourseActivityRoomOccupied(activity)) {
        val errMsg = new StringBuilder()
        errMsg.append(" 周").append(activity.time.day)
          .append(" 第")
          .append(activity.getTime.getStartUnit)
          .append("小节-第")
          .append(activity.getTime.getEndUnit)
          .append("小节")
        roomsConflictInfo.addConflictInfo(activity.rooms, errMsg.toString)
      }
      val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(activity.lesson, activity.time)
      val freerooms = entityDao.search(scheduleRoomService.getFreeRoomsOfConditions(timeUnits)
        .where("classroom in (:rooms)", activity.rooms))
      for (room <- activity.rooms if !freerooms.contains(room) && !context.lessonOccupiedRooms.contains(room)) {
        roomsConflictInfo.addConflictInfo(room, "被排考或者教室借用占用")
      }
    }
    roomsConflictInfo
  }
}
