package org.openurp.edu.eams.teach.schedule.service.impl

import java.util.Collection
import java.util.Collections
import java.util.Comparator
import java.util.List
import java.util.Set
import javax.servlet.http.HttpServletRequest
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Predicate
import org.apache.struts2.ServletActionContext
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.RequestUtils
import org.beangle.security.blueprint.SecurityUtils
import org.beangle.security.blueprint.User
import org.openurp.base.Room
import org.openurp.edu.eams.classroom.Occupancy
import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.eams.classroom.code.industry.RoomUsage
import org.openurp.edu.eams.classroom.model.OccupancyBean
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator.Usage
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.TimeUnitUtil
import org.openurp.edu.eams.teach.schedule.log.ScheduleLogBuilder
import org.openurp.edu.eams.teach.schedule.service.BruteForceArrangeContext
import org.openurp.edu.eams.teach.schedule.service.BruteForceArrangeContext.CommonConflictInfo
import org.openurp.edu.eams.teach.schedule.service.BruteForceArrangeService
import org.openurp.edu.eams.teach.schedule.service.CourseActivityService
import org.openurp.edu.eams.teach.schedule.service.ScheduleLogHelper
import org.openurp.edu.eams.teach.schedule.service.ScheduleRoomService

import scala.collection.JavaConversions._

class BruteForceArrangeServiceImpl extends BaseServiceImpl with BruteForceArrangeService {

  protected var courseActivityService: CourseActivityService = _

  protected var scheduleRoomService: ScheduleRoomService = _

  protected var scheduleLogHelper: ScheduleLogHelper = _

  def bruteForceArrange(context: BruteForceArrangeContext, rooms: Collection[Classroom]) {
    val lesson = context.getLesson
    val filteredRooms = CollectUtils.newArrayList(CollectionUtils.select(rooms, new Predicate() {

      def evaluate(`object`: AnyRef): Boolean = {
        var room = `object`.asInstanceOf[Classroom]
        return room.getCampus == lesson.getCampus && 
          room.getCapacity >= lesson.getTeachClass.getLimitCount
      }
    }))
    Collections.sort(filteredRooms, new Comparator[Classroom]() {

      def compare(o1: Classroom, o2: Classroom): Int = return o1.getCapacity - o2.getCapacity
    })
    if (CollectUtils.isEmpty(filteredRooms)) {
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

  private def saveActivities(context: BruteForceArrangeContext, activities: Collection[CourseActivity]) {
    val lesson = context.getLesson
    val isUpdateOperation = lesson.getCourseSchedule.getActivities.size > 1
    val alterationBefore = CourseActivityDigestor.getInstance.digest(null, lesson)
    val occupancies = CollectUtils.newHashSet()
    var period = 0
    for (activity <- activities) {
      period += (activity.getTime.getEndUnit - activity.getTime.getStartUnit + 
        1) * 
        Strings.count(activity.getTime.getWeekState, "1")
      val timeUnits = TimeUnitUtil.convertToTimeUnits(lesson, activity.getTime)
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
    lesson.getCourseSchedule.getActivities.clear()
    courseActivityService.removeActivities(Array(lesson.getId), lesson.getSemester)
    lesson.getCourseSchedule.getActivities.addAll(activities)
    lesson.getCourseSchedule.setPeriod(period)
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
    if (context.isDetectTake) {
      val collisionTakes = courseActivityService.collisionTakes(context.getLesson, context.getTransientActivities)
      context.getTakeConflictInfo.addConflictInfo(collisionTakes, "与本任务上课时间冲突")
    }
    if (context.isDetectTeacher) {
      for (activity <- context.getTransientActivities if courseActivityService.isCourseActivityTeacherOccupied(activity)) {
        val errMsg = new StringBuilder()
        errMsg.append(" 周").append(activity.getTime.getWeekday)
          .append(" 第")
          .append(activity.getTime.getStartUnit)
          .append("小节-第")
          .append(activity.getTime.getEndUnit)
          .append("小节")
        context.getTeacherConflictInfo.addConflictInfo(activity.getTeachers, errMsg.toString)
      }
    }
  }

  private def detectRoomConflict(context: BruteForceArrangeContext, activity: CourseActivity): CommonConflictInfo[Classroom] = {
    val roomsConflictInfo = context.buildRoomsConflictInfo()
    if (context.isDetectRoom) {
      if (courseActivityService.isCourseActivityRoomOccupied(activity)) {
        val errMsg = new StringBuilder()
        errMsg.append(" 周").append(activity.getTime.getWeekday)
          .append(" 第")
          .append(activity.getTime.getStartUnit)
          .append("小节-第")
          .append(activity.getTime.getEndUnit)
          .append("小节")
        roomsConflictInfo.addConflictInfo(activity.getRooms, errMsg.toString)
      }
      val timeUnits = TimeUnitUtil.convertToTimeUnits(activity.getLesson, activity.getTime)
      val freerooms = entityDao.search(scheduleRoomService.getFreeRoomsOfConditions(timeUnits)
        .where("classroom in (:rooms)", activity.getRooms))
      for (room <- activity.getRooms if !freerooms.contains(room) && !context.getLessonOccupiedRooms.contains(room)) {
        roomsConflictInfo.addConflictInfo(room, "被排考或者教室借用占用")
      }
    }
    roomsConflictInfo
  }

  def setCourseActivityService(courseActivityService: CourseActivityService) {
    this.courseActivityService = courseActivityService
  }

  def setScheduleRoomService(scheduleRoomService: ScheduleRoomService) {
    this.scheduleRoomService = scheduleRoomService
  }

  def setScheduleLogHelper(scheduleLogHelper: ScheduleLogHelper) {
    this.scheduleLogHelper = scheduleLogHelper
  }
}
