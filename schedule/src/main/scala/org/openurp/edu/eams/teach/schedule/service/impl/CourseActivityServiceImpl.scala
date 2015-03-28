package org.openurp.edu.eams.teach.schedule.service.impl

import java.lang.reflect.InvocationTargetException
import java.sql.Date
import java.util.Calendar




import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.bean.transformers.PropertyTransformer
import org.beangle.commons.collection.Collections
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.BitStrings
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.openurp.base.Room
import org.openurp.edu.eams.base.CourseUnit
import org.openurp.base.Semester
import org.openurp.base.TimeSetting
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.eams.classroom.Occupancy
import org.beangle.commons.lang.time.YearWeekTime
import org.openurp.base.code.RoomUsage
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator.Usage
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.core.service.TimeSettingService
import org.openurp.edu.eams.system.msg.SystemMessage
import org.openurp.edu.eams.system.msg.SystemMessageType
import org.openurp.edu.eams.system.msg.model.MessageContentBean
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operator
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.eams.teach.lesson.ExamMonitor
import org.openurp.edu.teach.exam.ExamRoom
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.SemesterUtil
import org.openurp.edu.eams.teach.lesson.util.YearWeekTimeUtil
import org.openurp.edu.eams.teach.schedule.dao.CourseActivityDao
import org.openurp.edu.eams.teach.schedule.model.CollisionInfo
import org.openurp.edu.eams.teach.schedule.model.CollisionResource.ResourceType
import org.openurp.edu.eams.teach.schedule.model.CourseArrangeAlteration
import org.openurp.edu.eams.teach.schedule.service.CourseActivityService
import org.openurp.edu.eams.teach.schedule.service.ScheduleLogHelper



class CourseActivityServiceImpl extends BaseServiceImpl with CourseActivityService {

  protected var courseActivityDao: CourseActivityDao = _

  protected var timeSettingService: TimeSettingService = _

  protected var semesterService: SemesterService = _

  protected var lessonService: LessonService = _

  protected var scheduleLogHelper: ScheduleLogHelper = _

  def saveActivities(lessons: Iterable[Lesson]) {
    if (Collections.isNotEmpty(lessons)) {
      entityDao.saveOrUpdate(lessons)
    }
  }

  def removeActivities(lessons: Iterable[Lesson]) {
    if (!lessons.isEmpty) {
      courseActivityDao.removeActivities(lessons)
    }
  }

  def removeActivities(lessonIds: Array[Long], semester: Semester) {
    if (ArrayUtils.isNotEmpty(lessonIds) && semester != null) {
      var occupancies = Collections.newBuffer[Any]
      var lessonIdCondition = ""
      for (i <- 0 until lessonIds.length) {
        if (i > 0) {
          lessonIdCondition += ","
        }
        lessonIdCondition += Strings.concat("'", String.valueOf(lessonIds(i)), "@", Usage.COURSE.toString, 
          "'")
      }
      val builder = OqlBuilder.from(classOf[Occupancy], "occupancy").where("occupancy.userid in (" + lessonIdCondition + ")")
        .where("occupancy.usage.id = :usageId", RoomUsage.COURSE)
      occupancies = entityDao.search(builder)
      val lessons = entityDao.get(classOf[Lesson], lessonIds)
      for (lesson <- lessons) {
        lesson.getCourseSchedule.setStatus(CourseStatusEnum.NEED_ARRANGE)
        lesson.getCourseSchedule.getActivities.clear()
        lesson.getCourseSchedule.setPeriod(0)
      }
      entityDao.execute(Operation.remove(occupancies).saveOrUpdate(lessons))
    }
  }

  def setCourseActivityDao(courseActivityDao: CourseActivityDao) {
    this.courseActivityDao = courseActivityDao
  }

  def getCourseUnits(lessonId: java.lang.Long, date: Date): List[CourseUnit] = {
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    if (null == lesson) {
      Collections.newBuffer[Any]
    } else {
      val semester = lesson.getSemester
      val timeCourseUnits = Collections.newSet[Any]
      if (!semester.contains(date)) {
        return Collections.newBuffer[Any]
      }
      val units = Array.ofDim[Boolean](timeCourseUnits.size)
      for (activity <- lesson.getCourseSchedule.getActivities if activity.contains(date); i <- activity.getTime.getStartUnit until activity.getTime.getEndUnit + 1) {
        units(i - 1) = true
      }
      val courseUnits = Collections.newBuffer[Any]
      for (courseUnit <- timeCourseUnits if units(courseUnit.getIndexno - 1)) courseUnits.add(courseUnit)
      Collections.sort(courseUnits)
      courseUnits
    }
  }

  def detectCollision[T](semester: Semester, `type`: ResourceType, timeSetting: TimeSetting): Iterable[CollisionInfo] = {
    detectCollision(semester, `type`, timeSetting, null)
  }

  def detectCollision[T](semester: Semester, 
      `type`: ResourceType, 
      timeSetting: TimeSetting, 
      lessonId: java.lang.Long): Iterable[CollisionInfo] = {
    var entityClass: Class[_ <: Entity[_ <: Number]] = null
    var query: OqlBuilder[_] = null
    val collisionInfos = Collections.newMap[Any]
    val collisionInfos2 = Collections.newMap[Any]
    val collisionInfos3 = Collections.newMap[Any]
    `type` match {
      case ADMINCLASS => 
        var resources = beforeDetectCollision(semester, `type`)
        entityClass = classOf[Adminclass]
        var params = Collections.newMap[Any]
        var hql = "select activity.lesson,activity1.lesson,activity.time from " + 
          classOf[CourseActivity].getName + 
          " activity," + 
          classOf[CourseActivity].getName + 
          " activity1 " + 
          "where activity.lesson.semester = :semester and activity1.lesson.semester=activity.lesson.semester " + 
          "and activity.lesson <> activity1.lesson and activity1.time.day=activity.time.day and " + 
          "activity1.time.startUnit <= activity.time.endUnit and activity1.time.endUnit >= activity.time.startUnit and " + 
          "bitand(activity1.time.state,activity.time.state)>0 "
        params.put("semester", semester)
        if (lessonId != null) {
          hql += "and activity.lesson.id = :lessonId"
          params.put("lessonId", lessonId)
        }
        var infos = entityDao.search(hql, params)
        for (info <- infos) {
          val lesson1 = info(0).asInstanceOf[Lesson]
          val lesson2 = info(1).asInstanceOf[Lesson]
          for (adminclassId <- resources.keySet if resources.get(adminclassId).contains(lesson1) && resources.get(adminclassId).contains(lesson2)) {
            var collisionInfo = collisionInfos.get(adminclassId.toString)
            if (null == collisionInfo) {
              collisionInfo = new CollisionInfo(adminclassId, info(2).asInstanceOf[CourseTime], "排课")
              collisionInfos.put(adminclassId.toString, collisionInfo)
            } else {
              collisionInfo.add(info(2).asInstanceOf[CourseTime])
            }
            //break
          }
        }
        fillCollisionEntities(entityClass, semester, collisionInfos)

      case CLASSROOM => 
        entityClass = classOf[Room]
        var timeAndRooms = getActivityTimes(semester, timeSetting, lessonId)
        for (objs <- timeAndRooms) {
          val courseTime = objs(0).asInstanceOf[CourseTime]
          val occupancyRoom = objs(1).asInstanceOf[Room]
          val timeUnits = YearWeekTimeUtil.convertToYearWeekTimes(semester, courseTime)
          params = Collections.newMap[Any]
          for (timeUnit <- timeUnits) {
            hql = "exists(select occupancy.room.id from org.openurp.edu.eams.classroom.Occupancy occupancy " + 
              "where bitand(occupancy.time.state," + 
              new java.lang.Long(timeunit.state) + 
              ")>0 and occupancy.time.day = :weekday" + 
              " and occupancy.time.year = :year and occupancy.time.start < :endTime" + 
              " and occupancy.time.end > :startTime and occupancy.room=classroom and occupancy.userid like '%" + 
              Usage.COURSE.toString + 
              "%'" + 
              " and occupancy.room = :occupancyRoom group by occupancy.room.id having count(*)>1)"
            val hql2 = "exists(select occupancy.room.id from org.openurp.edu.eams.classroom.Occupancy occupancy " + 
              "where bitand(occupancy.time.state," + 
              new java.lang.Long(timeunit.state) + 
              ")>0 and occupancy.time.day = :weekday" + 
              " and occupancy.time.year = :year and occupancy.time.start < :endTime" + 
              " and occupancy.time.end > :startTime and occupancy.room=occupancy1.room and occupancy.userid like '%" + 
              Usage.EXAM.toString + 
              "%'" + 
              " and occupancy.room = :occupancyRoom)"
            val hql3 = "exists(select occupancy.room.id from org.openurp.edu.eams.classroom.Occupancy occupancy " + 
              "where bitand(occupancy.time.state," + 
              new java.lang.Long(timeunit.state) + 
              ")>0 and occupancy.time.day = :weekday" + 
              " and occupancy.time.year = :year and occupancy.time.start < :endTime" + 
              " and occupancy.time.end > :startTime and occupancy.room=occupancy1.room and occupancy.userid like '%" + 
              Usage.OTHER.toString + 
              "%'" + 
              " and occupancy.room = :occupancyRoom group by occupancy.room.id)"
            query = OqlBuilder.from(classOf[Room], "classroom")
            query.where(hql)
            val query1 = OqlBuilder.from(classOf[Occupancy], "occupancy1")
            query1.where(hql2)
            query1.where("occupancy1.userid like '%" + Usage.COURSE.toString + 
              "'")
            val query2 = OqlBuilder.from(classOf[Occupancy], "occupancy1")
            query2.where(hql3)
            query2.where("occupancy1.userid like '%" + Usage.COURSE.toString + 
              "'")
            params.put("weekday", new java.lang.Integer(timeUnit.day))
            params.put("endTime", new java.lang.Integer(timeUnit.end))
            params.put("startTime", new java.lang.Integer(timeUnit.start))
            params.put("year", new java.lang.Integer(timeunit.year))
            params.put("occupancyRoom", occupancyRoom)
            query.params(params)
            query1.params(params)
            query2.params(params)
            val rooms = entityDao.search(query).asInstanceOf[List[Room]]
            for (room <- rooms) {
              var collisionInfo = collisionInfos.get(room.id.toString)
              if (null == collisionInfo) {
                collisionInfo = new CollisionInfo(room, courseTime, "排课")
                collisionInfos.put(room.id.toString, collisionInfo)
              } else {
                collisionInfo.add(courseTime)
              }
            }
            var occupancies = entityDao.search(query1).asInstanceOf[List[Occupancy]]
            for (occupancy <- occupancies) {
              var collisionInfo = collisionInfos2.get(occupancy.getRoom.id.toString)
              if (null == collisionInfo) {
                collisionInfo = new CollisionInfo(occupancy.getRoom, courseTime, "排考")
                collisionInfos2.put(occupancy.getRoom.id.toString, collisionInfo)
              } else {
                collisionInfo.add(courseTime)
              }
            }
            occupancies = entityDao.search(query2).asInstanceOf[List[Occupancy]]
            for (occupancy <- occupancies) {
              var collisionInfo = collisionInfos3.get(occupancy.getRoom.id.toString)
              if (null == collisionInfo) {
                collisionInfo = new CollisionInfo(occupancy.getRoom, courseTime, "教室借用")
                collisionInfos3.put(occupancy.getRoom.id.toString, collisionInfo)
              } else {
                collisionInfo.add(courseTime)
              }
            }
          }
        }
        fillCollisionEntities(entityClass, semester, collisionInfos, collisionInfos2, collisionInfos3)

      case TEACHER => 
        entityClass = classOf[Teacher]
        query = OqlBuilder.from(classOf[CourseActivity], "activity")
        query.select("select distinct teacher.id,activity.time")
        query.where("activity.lesson.semester = :semester")
        query.join("activity.teachers", "teacher")
        query.where("exists(from org.openurp.edu.teach.schedule.CourseActivity " + 
          "activity1 join activity1.teachers as teacher1 " + 
          "where activity1.lesson <> activity.lesson and " + 
          "teacher1 = teacher and " + 
          "activity1.id <>activity.id and activity1.time.day=activity.time.day and " + 
          "activity1.time.startUnit <= activity.time.endUnit and activity1.time.endUnit >= activity.time.startUnit and " + 
          "activity1.lesson.semester = :semester and " + 
          "bitand(activity1.time.state,activity.time.state)>0)")
        if (lessonId != null) {
          query.where("activity.lesson.id = :lessonId")
          query.param("lessonId", lessonId)
        }
        query.param("semester", semester)
        infos = entityDao.search(query).asInstanceOf[List[Array[Any]]]
        for (info <- infos) {
          if (null == info(0)) {
            //continue
          }
          var collisionInfo = collisionInfos.get(info(0).toString)
          if (null == collisionInfo) {
            collisionInfo = new CollisionInfo(info(0), info(1).asInstanceOf[CourseTime], "授课")
            collisionInfos.put(info(0).toString, collisionInfo)
          } else {
            collisionInfo.add(info(1).asInstanceOf[CourseTime])
          }
        }
        var activities = entityDao.get(classOf[ExamRoom], "semester", semester)
        if (!activities.isEmpty) {
          val infos2 = Collections.newSet[Any]
          for (examRoom <- activities) {
            val teachers = Collections.newSet[Any]
            teachers.add(examRoom.getExaminer)
            for (monitor <- examRoom.getMonitors) {
              teachers.add(monitor.getTeacher)
            }
            if (!teachers.isEmpty) {
              val time = YearWeekTimeUtil.buildCourseTime(examRoom.getStartAt, examRoom.getEndAt, examRoom.getSemester)
              val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
              builder.select("select distinct teacher.id,activity.time")
              builder.where("activity.lesson.semester = :semester", semester)
              builder.join("activity.teachers", "teacher")
              builder.where("teacher in (:teachers)", teachers)
              builder.where("bitand(" + time.state + ",activity.time.state)>0")
              builder.where("activity.time.day = :weekDay", time.day)
              builder.where("activity.time.start < :endTime", time.end)
              builder.where("activity.time.end > :startTime", time.start)
              infos2.addAll(entityDao.search(builder).asInstanceOf[Iterable[Array[Any]]])
            }
          }
          for (info <- infos2) {
            if (null == info(0)) {
              //continue
            }
            var collisionInfo = collisionInfos2.get(info(0).toString)
            if (null == collisionInfo) {
              collisionInfo = new CollisionInfo(info(0), info(1).asInstanceOf[CourseTime], "监考")
              collisionInfos2.put(info(0).toString, collisionInfo)
            } else {
              collisionInfo.add(info(1).asInstanceOf[CourseTime])
            }
          }
        }
        fillCollisionEntities(entityClass, semester, collisionInfos, collisionInfos2)

      case _ => Collections.emptyList()
    }
  }

  private def getActivityTimes(semester: Semester, timeSetting: TimeSetting, lessonId: java.lang.Long): List[Array[Any]] = {
    val query = OqlBuilder.from(classOf[CourseActivity], "activity")
    query.where("activity.lesson.semester = :semester", semester)
    if (null != lessonId) {
      query.where("activity.lesson.id = :lessonId", lessonId)
    }
    val hql = new StringBuilder()
    hql.append("select distinct activity.time,room")
    query.join("activity.rooms", "room")
    query.select(hql.toString)
    val orderBy = new StringBuilder()
    orderBy.append("activity.time.day,")
    orderBy.append("activity.time.startUnit,")
    orderBy.append("activity.time.endUnit,")
    orderBy.append("activity.time.state")
    query.orderBy(orderBy.toString)
    entityDao.search(query).asInstanceOf[List[Array[Any]]]
  }

  private def beforeDetectCollision(semester: Semester, `type`: ResourceType): Map[Integer, Set[Lesson]] = {
    val collisionResources = Collections.newMap[Any]
    val collisionResource = Collections.newBuffer[Any]
    val query = OqlBuilder.from(classOf[LessonLimitItem], "lessonLimitItem")
      .where("lessonLimitItem.group.lesson.semester = :semester", semester)
      .where("lessonLimitItem.meta.id = :metaId", if (ResourceType.ADMINCLASS == `type`) LessonLimitMeta.Adminclass.getMetaId else LessonLimitMeta.Program.getMetaId)
      .where("lessonLimitItem.operator =:operator1 or lessonLimitItem.operator =:operator2", Operator.IN, 
      Operator.Equals)
      .select("distinct lessonLimitItem.group.lesson,lessonLimitItem.content")
    val collisionResourceClass = entityDao.search(query).asInstanceOf[List[Array[Any]]]
    for (objects <- collisionResourceClass) {
      val adminclassIds = Strings.splitToInt(objects(1).asInstanceOf[String])
      if (ArrayUtils.isNotEmpty(adminclassIds)) {
        for (adminclassId <- adminclassIds) {
          collisionResource.add(Array(objects(0), adminclassId))
        }
      }
    }
    for (objects <- collisionResource) {
      val adminclassId = objects(1).asInstanceOf[java.lang.Integer]
      val lesson = objects(0).asInstanceOf[Lesson]
      var conflictLessons = collisionResources.get(adminclassId)
      if (conflictLessons == null) {
        conflictLessons = Collections.newHashSet(lesson)
        collisionResources.put(adminclassId, conflictLessons)
      } else {
        conflictLessons.add(lesson)
      }
    }
    collisionResources
  }

  private def fillCollisionEntities[T <: Entity[_ <: Number]](entityClass: Class[T], semester: Semester, collisionInfos: Map[String, CollisionInfo]*): Iterable[CollisionInfo] = {
    if (ArrayUtils.isEmpty(collisionInfos)) {
      return Collections.newBuffer[Any]
    }
    val collisionInfoResults = Collections.newSet[Any]
    val nClass = entityClass
    for (collisionInfosMap <- collisionInfos) {
      if (collisionInfosMap.isEmpty) {
        //continue
      }
      val idSet = collisionInfosMap.keySet
      val idClazz = Model.getType(nClass).idType
      val rsList = Collections.newBuffer[Any]
      for (a <- idSet) {
        rsList.add(DefaultConversion.Instance.convert(a, idClazz))
      }
      val collisionResources = entityDao.search(OqlBuilder.from(nClass, "clazz").where("clazz.id in (:ids)", 
        rsList))
      for (entity <- collisionResources) {
        val collisionInfo = collisionInfosMap.get(entity.id.toString)
        collisionInfo.setResource(entity)
        collisionInfo.mergeTimes()
        collisionInfoResults.add(collisionInfo)
      }
    }
    collisionInfoResults
  }

  def collisionTakes(lesson: Lesson, activities: Iterable[CourseActivity]): Iterable[CourseTake] = {
    if (Collections.isNotEmpty(lesson.getTeachClass.getCourseTakes)) {
      val stdIds = Collections.collect(lesson.getTeachClass.getNormalCourseTakes, new PropertyTransformer("std.id"))
      if (!stdIds.isEmpty) {
        val collisionTakes = Collections.newBuffer[Any]
        for (courseActivity <- activities if isStdsOccupied(courseActivity.getTime, stdIds, lesson); 
             stdId <- stdIds) {
          val courseTakes = getCourseTakes(stdId, courseActivity.getTime, lesson.getSemester)
          for (take <- courseTakes if take.getLesson.id != lesson.id && take.isAttend) {
            collisionTakes.add(take)
          }
        }
        return collisionTakes
      }
    }
    Collections.newBuffer[Any]
  }

  def isStdsOccupied(time: CourseTime, stdIds: Iterable[Long], except: Lesson): Boolean = {
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.join("activity.lesson.teachClass.courseTakes", "take")
    builder.where("take.std.id in (:stdIds)", stdIds)
    builder.where("bitand(activity.time.state, :weekStateNum) > 0", time.state)
    builder.where("activity.time.day = :weekday", time.day)
    builder.where("activity.time.startUnit <= :endUnit", time.getEndUnit)
    builder.where("activity.time.endUnit >= :startUnit", time.getStartUnit)
    builder.where("activity.lesson <> :lesson", except)
    builder.where("activity.lesson.semester = :semester", except.getSemester)
    val courseActivities = entityDao.search(builder)
    Collections.isNotEmpty(courseActivities)
  }

  def getCourseTakes(stdId: java.lang.Long, time: CourseTime, semester: Semester): List[CourseTake] = {
    if (null == stdId) {
      return Collections.newBuffer[Any]
    }
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    query.where("take.std.id = :stdId", stdId)
    val conditions = Collections.newBuffer[Any]
    if (null != time) {
      if (0 != time.day) {
        conditions.add(new Condition("activity.time.day = :weekday", time.day))
      }
      if (0 != time.getEndUnit) {
        conditions.add(new Condition("activity.time.startUnit <= :endUnit", time.getEndUnit))
      }
      if (0 != time.getStartUnit) {
        conditions.add(new Condition("activity.time.endUnit >= :startUnit", time.getStartUnit))
      }
      if (0 != time.state) {
        conditions.add(new Condition("bitand(activity.time.state, :weekStateNum) > 0)", new java.lang.Long(time.state)))
      }
    }
    if (semester != null) {
      query.where("take.lesson.semester = :semester", semester)
    }
    if (Collections.isNotEmpty(conditions)) {
      query.join("take.lesson.schedule.activities", "activity")
      query.where(conditions)
    }
    entityDao.search(query)
  }

  def saveOrUpdateActivity(lesson: Lesson, 
      occupancies: Set[Occupancy], 
      alterationBeforeMsg: String, 
      canToMessage: java.lang.Boolean, 
      user: User, 
      remoteAddr: String) {
    lesson.getCourseSchedule.setStatus(CourseStatusEnum.ARRANGED)
    entityDao.execute(Operation.saveOrUpdate(lesson).saveOrUpdate(occupancies))
    if (Strings.isNotEmpty(alterationBeforeMsg)) {
      entityDao.refresh(lesson)
      val alteration = new CourseArrangeAlteration()
      alteration.setLessonId(lesson.id)
      alteration.setSemester(lesson.getSemester)
      alteration.setAlterationBefore(alterationBeforeMsg)
      val alterationAfter = CourseActivityDigestor.getInstance.digest(null, lesson)
      if (Objects.!=(alterationBeforeMsg, alterationAfter)) {
        alteration.setAlterationAfter(alterationAfter)
        alteration.setAlterBy(user)
        alteration.setAlterFrom(remoteAddr)
        alteration.setAlterationAt(new java.util.Date())
        entityDao.saveOrUpdate(alteration)
      }
    }
    sendMessage(lesson, canToMessage, user)
  }

  def saveOrUpdateActivityWithnoAlterInfos(lesson: Lesson, 
      occupancies: Set[Occupancy], 
      alterationBeforeMsg: String, 
      canToMessage: java.lang.Boolean, 
      user: User, 
      remoteAddr: String) {
    lesson.getCourseSchedule.setStatus(CourseStatusEnum.ARRANGED)
    entityDao.execute(Operation.saveOrUpdate(lesson).saveOrUpdate(occupancies))
  }

  def sendMessage(task: Lesson, canToMessage: java.lang.Boolean, user: User) {
    if (true == canToMessage) {
      val strMsg = new StringBuilder()
      strMsg.append(task.getSemester.getSchoolYear)
      strMsg.append(" ")
      strMsg.append(task.getSemester.getName)
      strMsg.append("，序号为：")
      strMsg.append(task.getNo)
      strMsg.append("《")
      strMsg.append(task.getCourse.getName)
      strMsg.append("》")
      strMsg.append("课程排课结果有变动，请及时查看调整后的结果。本消息为系统自动提醒功能，请勿回复！")
      val content = Model.newInstance(classOf[MessageContentBean])
      content.setSender(user)
      content.setSubject("调课通知！")
      val messages = Collections.newSet[Any]
      content.setText(strMsg.toString)
      content.setCreatedAt(new java.util.Date())
      content.setActiveOn(content.getCreatedAt)
      val c = Calendar.getInstance
      c.setTime(content.getActiveOn)
      c.add(Calendar.DAY_OF_WEEK, 15)
      content.setInvalidateOn(c.getTime)
      content.setMessages(messages)
      for (teacher <- task.getTeachers) {
        val message = Model.newInstance(classOf[SystemMessage])
        val users = entityDao.get(classOf[User], "name", teacher.getCode)
        if (Collections.isEmpty(users)) {
          //continue
        }
        message.setRecipient(users.get(0))
        message.setStatus(SystemMessageType.NEWLY)
        message.setContent(content)
        messages.add(message)
      }
      entityDao.saveOrUpdate(content)
    }
  }

  def isCourseActivityRoomOccupied(activity: CourseActivity): Boolean = {
    if (Collections.isEmpty(activity.getRooms)) {
      return false
    }
    val query = OqlBuilder.from(classOf[CourseActivity], "activity")
    query.where("activity.lesson != :lesson", activity.getLesson)
    query.join("activity.rooms", "room").where("room in (:rooms)", activity.getRooms)
    query.where("BITAND (activity.time.state," + activity.getTime.state + 
      ") > 0")
    query.where("activity.time.day = :weekday", activity.getTime.day)
    query.where("activity.time.startUnit <= :endUnit", activity.getTime.getEndUnit)
    query.where("activity.time.endUnit >= :startUnit", activity.getTime.getStartUnit)
    query.where("exists(from org.openurp.edu.teach.lesson.Lesson lesson2 where activity.lesson=lesson2 and lesson2.semester=:semester)", 
      activity.getLesson.getSemester)
    query.select("distinct activity")
    Collections.isNotEmpty(entityDao.search(query))
  }

  def isCourseActivityTeacherOccupied(activity: CourseActivity): Boolean = {
    if (Collections.isEmpty(activity.getTeachers)) {
      return false
    }
    val query = OqlBuilder.from(classOf[CourseActivity], "activity")
    query.where("activity.lesson != :lesson", activity.getLesson)
    query.join("activity.teachers", "teacher").where("teacher in (:teachers)", activity.getTeachers)
    query.where("BITAND (activity.time.state," + activity.getTime.state + 
      ") > 0")
    query.where("activity.time.day = :weekday", activity.getTime.day)
    query.where("activity.time.startUnit <= :endUnit", activity.getTime.getEndUnit)
    query.where("activity.time.endUnit >= :startUnit", activity.getTime.getStartUnit)
    query.where("exists(from org.openurp.edu.teach.lesson.Lesson lesson2 where activity.lesson=lesson2 and lesson2.semester=:semester)", 
      activity.getLesson.getSemester)
    query.select("distinct activity")
    Collections.isNotEmpty(entityDao.search(query))
  }

  def shift(lesson: Lesson, 
      offset: Int, 
      canToMessage: java.lang.Boolean, 
      user: User) {
    if (0 == offset) {
      return
    }
    val from = lesson.getSemester
    val fromWeekStart = 1
    val fromWeekEnd = 1 + from.getWeeks
    val fromYear = SemesterUtil.getStartYear(from)
    entityDao.saveOrUpdate(shift(lesson.getCourseSchedule.getActivities, fromYear, fromWeekStart, fromWeekEnd, 
      fromWeekStart + offset, fromWeekEnd + offset, null, from))
    var startWeek = lesson.getCourseSchedule.getStartWeek + offset
    var endWeek = lesson.getCourseSchedule.getEndWeek + offset
    if (startWeek <= 0) {
      startWeek = 1
    }
    if (endWeek <= 0) {
      endWeek = 1
    }
    if (startWeek >= from.getWeeks) {
      startWeek = from.getWeeks
    }
    if (endWeek >= from.getWeeks) {
      endWeek = from.getWeeks
    }
    lesson.getCourseSchedule.setWeekState(WeekStates.build(startWeek + "-" + endWeek))
    entityDao.saveOrUpdate(lesson)
  }

  def shift(activities: Iterable[CourseActivity], 
      fromYear: Int, 
      fromWeekStart: Int, 
      fromWeekEnd: Int, 
      toWeekStart: Int, 
      toWeekEnd: Int, 
      timeSetting: TimeSetting, 
      to: Semester): Iterable[Entity[Long]] = {
    val keeped = Collections.newBuffer[Any]
    for (activity <- activities) {
      processShift(keeped, activity, fromWeekStart, fromWeekEnd, toWeekStart, toWeekEnd, timeSetting, 
        to)
    }
    keeped
  }

  protected def processShift(keeped: List[Entity[Long]], 
      activity: CourseActivity, 
      fromWeekStart: Int, 
      fromWeekEnd: Int, 
      toWeekStart: Int, 
      toWeekEnd: Int, 
      timeSetting: TimeSetting, 
      to: Semester) {
    val time = activity.getTime
    val builder = OqlBuilder.from(classOf[Occupancy], "occupancy")
    builder.where("occupancy.usage.id = :usageId", RoomUsage.COURSE)
    builder.where("occupancy.userid = :userid", RoomUseridGenerator.gen(activity.getLesson, Usage.COURSE))
    val occupancies = entityDao.search(builder)
    val weekState = time.state
    var weeks = Strings.substring(weekState, fromWeekStart, fromWeekEnd)
    if (weeks.length < toWeekEnd - toWeekStart) {
      weeks += Strings.repeat("0", toWeekEnd - toWeekStart - weeks.length)
    } else {
      weeks = weeks.substring(0, toWeekEnd - toWeekStart)
    }
    var toWeeks = ""
    toWeeks = if (toWeekStart > 0) Strings.repeat("0", toWeekStart) + weeks else "0" + 
      weeks.substring(Math.abs(toWeekStart - fromWeekStart))
    if (!Strings.contains(toWeeks, '1')) {
      time.newWeekState(Strings.repeat("0", ExamYearWeekTimeUtil.OVERALLWEEKS))
      keeped.add(activity)
    } else {
      if (toWeeks.length > to.getWeeks + 1) {
        toWeeks = toWeeks.substring(0, to.getWeeks + 1)
      }
      time.newWeekState(toWeeks + 
        Strings.repeat("0", ExamYearWeekTimeUtil.OVERALLWEEKS - toWeeks.length))
      if (null != timeSetting) {
        time.setStartTime(timeSetting.getDefaultUnits.get(time.getStartUnit).start)
        time.setEndTime(timeSetting.getDefaultUnits.get(time.getEndUnit).end)
      }
      keeped.add(activity)
      val timeUnits2 = YearWeekTimeUtil.convertToYearWeekTimes(activity.getLesson, time)
      if (timeUnits2.length == 1 && occupancies.size == 1) {
        val occupancy = occupancies.get(0)
        occupancy.setTime(timeUnits2(0))
        keeped.add(occupancy)
      } else if (timeUnits2.length == 1 && occupancies.size == 2) {
        entityDao.remove(occupancies.get(1))
        val occupancy = occupancies.get(0)
        occupancy.setTime(timeUnits2(0))
        keeped.add(occupancy)
      } else if (timeUnits2.length == 2 && occupancies.size == 1) {
        val occupancy = occupancies.get(0)
        occupancy.setTime(timeUnits2(0))
        val occupancy2 = BeanUtils.cloneBean(occupancy).asInstanceOf[Occupancy]
        occupancy2.setId(null)
        occupancy2.setTime(timeUnits2(1))
        keeped.add(occupancy)
        keeped.add(occupancy2)
      } else if (timeUnits2.length == 2 && occupancies.size == 2) {
        for (timeUnit <- timeUnits2; occupancy <- occupancies if timeunit.year == occupancy.getTime.year) {
          occupancy.setTime(timeUnit)
          keeped.add(occupancy)
        }
      }
    }
  }

  def mergeActivites(tobeMerged: List[CourseActivity]): List[CourseActivity] = {
    val mergedActivityList = Collections.newBuffer[Any]
    if (Collections.isEmpty(tobeMerged)) return mergedActivityList
    Collections.sort(tobeMerged)
    val activityIter = tobeMerged.iterator()
    var toMerged = activityIter.next()
    mergedActivityList.add(toMerged)
    while (activityIter.hasNext) {
      val activity = activityIter.next()
      if (canMergerWith(toMerged, activity)) mergeWith(toMerged, activity) else {
        toMerged = activity
        mergedActivityList.add(toMerged)
      }
    }
    mergedActivityList
  }

  private def mergeWith(toMerged: CourseActivity, activity: CourseActivity) {
    val builder = OqlBuilder.from(classOf[Occupancy], "occupancy")
    builder.where("exists (from org.openurp.edu.teach.schedule.CourseActivity activity " + 
      "join activity.rooms room where room = occupancy.room and activity = :activity " + 
      "and occupancy.time.day = activity.time.day " + 
      "and occupancy.time.start = activity.time.start " + 
      "and occupancy.time.end = activity.time.end " + 
      "and activity.time.state = occupancy.time.state)", activity)
    val builder1 = OqlBuilder.from(classOf[Occupancy], "occupancy")
    builder1.where("exists (from org.openurp.edu.teach.schedule.CourseActivity activity " + 
      "join activity.rooms room where room = occupancy.room and activity = :activity " + 
      "and occupancy.time.day = activity.time.day " + 
      "and occupancy.time.start = activity.time.start " + 
      "and occupancy.time.end = activity.time.end " + 
      "and activity.time.state = occupancy.time.state)", toMerged)
    toMerged.getTime.newWeekState(BitStrings.or(toMerged.getTime.getWeekState, activity.getTime.getWeekState))
    val occupancies = entityDao.search(builder)
    val occupancies1 = entityDao.search(builder1)
    for (occupancy <- occupancies1) {
      occupancy.getTime.newWeekState(toMerged.getTime.getWeekState)
    }
    entityDao.execute(Operation.remove(occupancies).saveOrUpdate(occupancies1))
  }

  private def canMergerWith(toMerged: CourseActivity, activity: CourseActivity): Boolean = {
    if (toMerged.getTeachers != activity.getTeachers) {
      return false
    }
    if (toMerged.getRooms != activity.getRooms) {
      return false
    }
    if (toMerged.getTime.day != activity.getTime.day) {
      return false
    }
    if (toMerged.getTime.getStartUnit != toMerged.getTime.getStartUnit) {
      return false
    }
    if (toMerged.getTime.getEndUnit != activity.getTime.getEndUnit) {
      return false
    }
    val weekState = Strings.repeat("0", ExamYearWeekTimeUtil.OVERALLWEEKS)
    weekState == BitStrings.and(toMerged.getTime.getWeekState, activity.getTime.getWeekState)
  }

  def setTimeSettingService(timeSettingService: TimeSettingService) {
    this.timeSettingService = timeSettingService
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def setLessonService(lessonService: LessonService) {
    this.lessonService = lessonService
  }

  def setScheduleLogHelper(scheduleLogHelper: ScheduleLogHelper) {
    this.scheduleLogHelper = scheduleLogHelper
  }
}
