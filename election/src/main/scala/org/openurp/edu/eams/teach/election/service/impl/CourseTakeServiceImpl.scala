package org.openurp.edu.eams.teach.election.service.impl

import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.Collections
import java.util.Date
import java.util.List
import java.util.Map
import java.util.Set
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Throwables
import org.beangle.commons.text.i18n.Message
import org.beangle.ems.dictionary.service.BaseCodeService
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.base.CourseUnit
import org.openurp.edu.eams.base.Semester
import org.openurp.code.person.Gender
import org.openurp.edu.eams.base.util.WeekDays
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.StudentService
import org.openurp.edu.eams.system.msg.service.SystemMessageService
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.dao.ElectionDao
import org.openurp.edu.eams.teach.election.filter.CourseTakeFilterStrategy
import org.openurp.edu.eams.teach.election.model.CourseTakeInitMessage
import org.openurp.edu.eams.teach.election.model.ElectMailTemplate
import org.openurp.edu.eams.teach.election.model.Enum.AssignStdType
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.CourseTakeService
import org.openurp.edu.eams.teach.election.service.ElectLoggerService
import org.openurp.edu.eams.teach.election.service.context.CourseTakeStat
import org.openurp.edu.eams.teach.election.service.event.ElectCourseEvent
import org.openurp.edu.eams.teach.election.service.helper.CourseLimitGroupHelper
import org.openurp.edu.eams.teach.election.service.helper.FreeMarkerHelper
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseTakeBean
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.web.helper.AdminclassSearchHelper
import org.openurp.edu.eams.web.util.OutputObserver
import org.openurp.edu.eams.web.util.OutputProcessObserver
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class CourseTakeServiceImpl extends BaseServiceImpl with CourseTakeService {

  @BeanProperty
  var electLoggerService: ElectLoggerService = _

  private var courseLimitService: CourseLimitService = _

  private var adminclassSearchHelper: AdminclassSearchHelper = _

  private var studentService: StudentService = _

  protected var electionDao: ElectionDao = _

  @BeanProperty
  var systemMessageService: SystemMessageService = _

  private var baseCodeService: BaseCodeService = _

  protected var courseTakeFilterStrategy: CourseTakeFilterStrategy = _

  def election(student: Student, 
      existedTakes: Collection[CourseTake], 
      lessonCollection: Collection[Lesson], 
      unCheckTimeConflict: Boolean): List[Message] = {
    synchronized {
      val date = new Date()
      val results = CollectUtils.newArrayList()
      var successCount = 0
      var failureCount = 0
      val successMsg = new Message("0")
      val failureMsg = new Message("0")
      results.add(successMsg)
      results.add(failureMsg)
      val failureMessages = CollectUtils.newHashMap()
      val lessonSet = CollectUtils.newHashSet(lessonCollection)
      val electedLessons = CollectUtils.newHashSet()
      val candidateLessons = CollectUtils.newHashSet()
      val successLessons = CollectUtils.newArrayList()
      if (!unCheckTimeConflict) {
        for (courseTake <- existedTakes if courseTake.getStd == student) {
          electedLessons.add(courseTake.getLesson)
        }
        for (lesson <- lessonSet) {
          if (electedLessons.contains(lesson)) {
            //continue
          }
          var failureMsgsSet = failureMessages.get(lesson)
          if (null == failureMsgsSet) {
            failureMsgsSet = CollectUtils.newHashSet()
          }
          for (l <- electedLessons) {
            if (lesson.getCourse.getCode == l.getCourse.getCode) {
              failureMsgsSet.add(" 已经存在" + l.getCourse.getName + "[" + l.getCourse.getCode + 
                "]的选课记录(已选课程序号:" + 
                l.getNo + 
                ")")
              //continue
            }
            val activities = lesson.getCourseSchedule.getActivities
            val activities2 = l.getCourseSchedule.getActivities
            for (courseActivity <- activities) {
              val time = courseActivity.getTime
              for (courseActivity2 <- activities2) {
                val time2 = courseActivity2.getTime
                if ((time.getWeekStateNum & time2.getWeekStateNum) > 0 && time.getWeekday == time2.getWeekday && 
                  time.getStartUnit <= time2.getEndUnit && 
                  time.getEndUnit >= time2.getStartUnit) {
                  val sb = new StringBuilder()
                  if (time.getWeekday == time2.getWeekday) {
                    sb.append(WeekDays.get(time.getWeekday).getName)
                  }
                  if (time.getStartUnit <= time2.getEndUnit && time.getEndUnit >= time2.getStartUnit) {
                    sb.append("第").append(Math.max(time.getStartUnit, time2.getStartUnit))
                      .append("-")
                      .append(Math.min(time.getEndUnit, time2.getEndUnit))
                      .append("小节")
                  }
                  sb.append("与 ").append(l.getCourse.getName).append("[")
                    .append(l.getNo)
                    .append("] ")
                  failureMsgsSet.add(sb.append("冲突").toString)
                }
              }
            }
          }
          if (failureMsgsSet.isEmpty) {
            candidateLessons.add(lesson)
          } else {
            failureCount += 1
            failureMessages.put(lesson, failureMsgsSet)
          }
        }
      } else {
        for (lesson <- lessonSet if !electedLessons.contains(lesson)) {
          candidateLessons.add(lesson)
        }
      }
      val courseTakeType = Model.newInstance(classOf[CourseTakeType], CourseTakeType.NORMAL)
      val electionMode = Model.newInstance(classOf[ElectionMode], ElectionMode.ASSIGEND)
      for (lesson <- candidateLessons) {
        val courseTake = genCourseTake(lesson, student, courseTakeType, electionMode, date)
        val logger = electLoggerService.genLogger(courseTake, ElectRuleType.ELECTION, null, date)
        try {
          lesson.getTeachClass.setStdCount(lesson.getTeachClass.getStdCount + 1)
          val limitGroup = CourseLimitGroupHelper.getMatchCourseLimitGroup(lesson, student)
          if (limitGroup != null) {
            limitGroup.setCurCount(limitGroup.getCurCount + 1)
            courseTake.setLimitGroup(limitGroup)
          }
          entityDao.save(courseTake, logger)
          publish(ElectCourseEvent.create(courseTake))
          successCount += 1
          successLessons.add(lesson)
        } catch {
          case e: Exception => {
            failureCount += 1
            failureMessages.put(lesson, CollectUtils.newHashSet("添加失败"))
          }
        }
      }
      successMsg.setKey(successCount + "")
      successMsg.getParams.add(successLessons)
      failureMsg.setKey(failureCount + "")
      failureMsg.getParams.add(failureMessages)
      results
    }
  }

  def genCourseTake(lesson: Lesson, 
      std: Student, 
      courseTakeType: CourseTakeType, 
      electionMode: ElectionMode, 
      date: Date): CourseTake = {
    val courseTake = new CourseTakeBean()
    courseTake.setLesson(lesson)
    courseTake.setStd(std)
    courseTake.setCourseTakeType(courseTakeType)
    courseTake.setElectionMode(electionMode)
    courseTake.setCreatedAt(date)
    courseTake.setUpdatedAt(date)
    courseTake
  }

  def election(students: Collection[Student], 
      electedCourseTakes: Collection[CourseTake], 
      lesson: Lesson, 
      unCheckTimeConflict: Boolean): List[Message] = {
    synchronized {
      val date = new Date()
      val results = CollectUtils.newArrayList()
      val successMsg = new Message("0")
      val failureMsg = new Message("0")
      var successCount = 0
      var failureCount = 0
      val failureMessages = CollectUtils.newHashMap()
      val successStds = CollectUtils.newArrayList()
      results.add(successMsg)
      results.add(failureMsg)
      val courseTakeMap = CollectUtils.newHashMap()
      val stds = CollectUtils.newHashSet()
      for (courseTake <- electedCourseTakes) {
        var stdElectedLessons = courseTakeMap.get(courseTake.getStd)
        if (null == stdElectedLessons) {
          stdElectedLessons = CollectUtils.newHashSet()
        }
        stdElectedLessons.add(courseTake.getLesson)
        courseTakeMap.put(courseTake.getStd, stdElectedLessons)
      }
      if (!unCheckTimeConflict) {
        for (student <- students) {
          val stdElectedLessons = courseTakeMap.get(student)
          if (null == stdElectedLessons || stds.contains(student)) {
            stds.add(student)
            //continue
          }
          var failureMsgsSet = failureMessages.get(lesson)
          if (null == failureMsgsSet) {
            failureMsgsSet = CollectUtils.newHashSet()
          }
          for (l <- stdElectedLessons) {
            if (lesson == l) {
              //break
            } else if (lesson.getCourse.getCode == l.getCourse.getCode) {
              failureMsgsSet.add(" 已经存在" + l.getCourse.getName + "[" + l.getCourse.getCode + 
                "]的选课记录(已选课程序号:" + 
                l.getNo + 
                ")")
              //continue
            }
            val activities = lesson.getCourseSchedule.getActivities
            val activities2 = l.getCourseSchedule.getActivities
            for (courseActivity <- activities) {
              val time = courseActivity.getTime
              for (courseActivity2 <- activities2) {
                val time2 = courseActivity2.getTime
                if ((time.getWeekStateNum & time2.getWeekStateNum) > 0 && time.getWeekday == time2.getWeekday && 
                  time.getStartUnit <= time2.getEndUnit && 
                  time.getEndUnit >= time2.getStartUnit) {
                  val sb = new StringBuilder()
                  if (time.getWeekday == time2.getWeekday) {
                    sb.append(WeekDays.get(time.getWeekday).getName)
                  }
                  if (time.getStartUnit <= time2.getEndUnit && time.getEndUnit >= time2.getStartUnit) {
                    sb.append("第").append(Math.max(time.getStartUnit, time2.getStartUnit))
                      .append("-")
                      .append(Math.min(time.getEndUnit, time2.getEndUnit))
                      .append("小节")
                  }
                  failureMsgsSet.add(sb.append("冲突").toString)
                }
              }
            }
          }
          if (failureMsgsSet.isEmpty) {
            stds.add(student)
          } else {
            failureCount += 1
            failureMessages.put(student, failureMsgsSet)
          }
        }
      } else {
        for (student <- students if !courseTakeMap.get(student).contains(lesson)) {
          stds.add(student)
        }
      }
      val courseTakeType = Model.newInstance(classOf[CourseTakeType], CourseTakeType.NORMAL)
      val electionMode = Model.newInstance(classOf[ElectionMode], ElectionMode.ASSIGEND)
      for (student <- stds) {
        val courseTake = genCourseTake(lesson, student, courseTakeType, electionMode, date)
        val logger = electLoggerService.genLogger(courseTake, ElectRuleType.ELECTION, null, date)
        try {
          lesson.getTeachClass.setStdCount(lesson.getTeachClass.getStdCount + 1)
          val limitGroup = CourseLimitGroupHelper.getMatchCourseLimitGroup(lesson, student)
          if (limitGroup != null) {
            limitGroup.setCurCount(limitGroup.getCurCount + 1)
            courseTake.setLimitGroup(limitGroup)
          }
          entityDao.save(courseTake, logger)
          publish(ElectCourseEvent.create(courseTake))
          successCount += 1
          successStds.add(student)
        } catch {
          case e: Exception => {
            failureCount += 1
            failureMessages.put(student, CollectUtils.newHashSet("添加失败"))
          }
        }
      }
      successMsg.setKey(successCount + "")
      successMsg.getParams.add(successStds)
      failureMsg.setKey(failureCount + "")
      failureMsg.getParams.add(failureMessages)
      results
    }
  }

  def filter(amount: Int, takes: List[CourseTake], params: Map[String, Any]): List[Message] = {
    val toBeRemoved = courseTakeFilterStrategy.getToBeRemoved(amount, takes, params)
    val stdId2Messages = CollectUtils.newHashMap()
    for (courseTake <- toBeRemoved) {
      var messages = stdId2Messages.get(courseTake.getStd.getId)
      if (null == messages) {
        messages = CollectUtils.newArrayList()
        stdId2Messages.put(courseTake.getStd.getId, messages)
      }
      val std = courseTake.getStd
      val lesson = courseTake.getLesson
      try {
        val result = electionDao.removeElection(courseTake, true)
        courseTakeFilterStrategy.postFilter(courseTake)
        if (result == 0) {
          messages.add(new Message(std.getName + "[" + std.getCode + "] 的 " + lesson.getCourse.getName + 
            "[" + 
            lesson.getNo + 
            "] 退课成功", Array(true)))
        } else {
          messages.add(new Message(std.getName + "[" + std.getCode + "] 的 " + lesson.getCourse.getName + 
            "[" + 
            lesson.getNo + 
            "] 退课失败", Array(false)))
        }
      } catch {
        case e: Exception => {
          logger.error(Throwables.getStackTrace(e))
          messages.add(new Message(std.getName + "[" + std.getCode + "] 的 " + lesson.getCourse.getName + 
            "[" + 
            lesson.getNo + 
            "] 退课失败", Array(false)))
        }
      }
    }
    val result = CollectUtils.newArrayList()
    for (messages <- stdId2Messages.values) {
      result.addAll(messages)
    }
    result
  }

  def withdraw(courseTakes: List[CourseTake], sender: User): List[Message] = {
    synchronized {
      val template = if (null == sender) null else entityDao.get(classOf[ElectMailTemplate], ElectMailTemplate.WITHDRAW)
      val stdId2Messages = CollectUtils.newHashMap()
      for (courseTake <- courseTakes) {
        var messages = stdId2Messages.get(courseTake.getStd.getId)
        if (null == messages) {
          messages = CollectUtils.newArrayList()
          stdId2Messages.put(courseTake.getStd.getId, messages)
        }
        val std = courseTake.getStd
        val lesson = courseTake.getLesson
        try {
          val result = electionDao.removeElection(courseTake, true)
          if (result == 0) {
            messages.add(new Message(std.getName + "[" + std.getCode + "] 的 " + lesson.getCourse.getName + 
              "[" + 
              lesson.getNo + 
              "] 退课成功", Array(true)))
            if (null != template) {
              try {
                val contentTemplate = FreeMarkerHelper.dynamicCompileTemplate(template, courseTake)
                systemMessageService.sendSimpleMessage(contentTemplate.getTitle, contentTemplate.getContent, 
                  sender.getName, std.getCode)
              } catch {
                case e: Exception => {
                  logger.error(Throwables.getStackTrace(e))
                  messages.add(new Message(std.getName + "[" + std.getCode + "] 的 " + lesson.getCourse.getName + 
                    "[" + 
                    lesson.getNo + 
                    "] 退课消息发送失败", Array(false)))
                }
              }
            }
          } else {
            messages.add(new Message(std.getName + "[" + std.getCode + "] 的 " + lesson.getCourse.getName + 
              "[" + 
              lesson.getNo + 
              "] 退课失败", Array(false)))
          }
        } catch {
          case e: Exception => {
            logger.error(Throwables.getStackTrace(e))
            messages.add(new Message(std.getName + "[" + std.getCode + "] 的 " + lesson.getCourse.getName + 
              "[" + 
              lesson.getNo + 
              "] 退课失败", Array(false)))
          }
        }
      }
      val result = CollectUtils.newArrayList()
      for (messages <- stdId2Messages.values) {
        result.addAll(messages)
      }
      result
    }
  }

  def getCourseTakes(students: Collection[Student], semester: Semester): Map[Student, List[CourseTake]] = {
    var courseTakes = CollectUtils.newArrayList()
    val result = CollectUtils.newHashMap()
    for (student <- students) {
      result.put(student, new ArrayList[CourseTake]())
    }
    if (null == semester) {
      courseTakes = entityDao.get(classOf[CourseTake], "std", students)
    } else {
      val builder = OqlBuilder.from(classOf[CourseTake].getName + " courseTake")
      builder.where("courseTake.lesson.semester=:semester and courseTake.std in(:stds)")
      val stdArray = students.toArray(Array())
      var i = 0
      while (i < stdArray.length) {
        var end = i + 500
        if (end > stdArray.length) {
          end = stdArray.length
        }
        val parameterMap = CollectUtils.newHashMap()
        parameterMap.put("stds", ArrayUtils.subarray(stdArray, i, end))
        parameterMap.put("semester", semester)
        courseTakes.addAll(entityDao.search(builder.params(parameterMap).build()))
        i += 500
      }
    }
    for (courseTake <- courseTakes) {
      var takes = result.get(courseTake.getStd)
      if (null == takes) {
        takes = CollectUtils.newArrayList()
        result.put(courseTake.getStd, takes)
      }
      takes.add(courseTake)
    }
    result
  }

  def getCourseTakes(student: Student, semesters: Semester*): List[CourseTake] = {
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    if (semesters.length > 0) {
      builder.where("courseTake.lesson.semester in(:semesters)", semesters)
    }
    builder.where("courseTake.std=:std", student)
    entityDao.search(builder)
  }

  def getCourseTakes(student: Student, semester: Semester, week: Int): List[CourseTake] = {
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    builder.where("courseTake.lesson.semester=:semester", semester)
    builder.where("courseTake.std=:std", student)
    builder.where("courseTake.lesson.courseSchedule.startWeek <=:week and courseTake.lesson.courseSchedule.endWeek >=:week", 
      week)
    entityDao.search(builder)
  }

  def getCourseTable(courseTakes: List[CourseTake], units: List[CourseUnit]): Array[Array[List[CourseTake]]] = {
    val courseTable = Array.ofDim[List[_]](units.get(units.size - 1).getIndexno, 7)
    for (courseTake <- courseTakes; courseActivity <- courseTake.getLesson.getCourseSchedule.getActivities) {
      var i = courseActivity.getTime.getStartUnit - 1
      while (i <= courseActivity.getTime.getEndUnit - 1) {
        if (null == 
          courseTable(i)(courseActivity.getTime.getWeekday - 1)) {
          courseTable(i)(courseActivity.getTime.getWeekday - 1) = CollectUtils.newArrayList()
        }
        courseTable(i)(courseActivity.getTime.getWeekday - 1)
          .add(courseTake)
        i += 1
      }
    }
    courseTable
  }

  def getCourseTable(student: Student, 
      semester: Semester, 
      week: Int, 
      units: List[CourseUnit]): Array[Array[List[CourseTake]]] = {
    val courseTakes = getCourseTakes(student, semester, week)
    getCourseTable(courseTakes, units)
  }

  def assignStds(tasks: Collection[Lesson], semester: Semester, observer: OutputObserver) {
    assignStds(tasks, AssignStdType.ALL, semester, observer)
  }

  def assignStds(lessons: Collection[Lesson], 
      `type`: AssignStdType, 
      semester: Semester, 
      observer: OutputObserver) {
    val outPutObserver = observer.asInstanceOf[OutputProcessObserver]
    try {
      outPutObserver.notifyStart(outPutObserver.messageOf("info.setCourseTake") + "(" + 
        lessons.size + 
        ")", lessons.size, null)
      val lessonIds = CollectUtils.newHashSet()
      for (lesson <- lessons) {
        lessonIds.add(lesson.getId)
        try {
          val count = assignStds(lesson, `type`)
          if (null != outPutObserver) {
            outPutObserver.outputNotify(OutputObserver.good, new CourseTakeInitMessage("info.init.courseTake.for", 
              lesson, String.valueOf(count)), true)
          }
        } catch {
          case e: Exception => try {
            e.printStackTrace()
            if (null != outPutObserver) {
              outPutObserver.outputNotify(OutputObserver.error, new CourseTakeInitMessage("error.init.courseTake.for", 
                lesson), true)
            }
          } catch {
            case e1: Exception => logger.info("IOException occurred when credit init scheme id:" + lesson.toString + 
              "\n and exception stack is " + 
              Throwables.getStackTrace(e))
          }
        }
      }
      if (!lessons.isEmpty) {
        val sb = new StringBuilder("update " + classOf[Lesson].getName + " lesson " + "set lesson.teachClass.stdCount =(" + 
          "select count(courseTake.id) from " + 
          classOf[CourseTake].getName + 
          " courseTake " + 
          "where courseTake.lesson.id=lesson.id) " + 
          "where lesson.id in (")
        for (i <- 0 until lessonIds.size) {
          sb.append("?" + (i + 1))
          if (i < lessonIds.size - 1) {
            sb.append(",")
          } else {
            sb.append(") ")
          }
        }
        val sb2 = new StringBuilder("update " + classOf[CourseLimitGroup].getName + " cg " + 
          "set cg.curCount =(" + 
          "select count(courseTake.id) from " + 
          classOf[CourseTake].getName + 
          " courseTake " + 
          "where courseTake.limitGroup.id=cg.id) where cg.lesson.id in (")
        for (i <- 0 until lessonIds.size) {
          sb2.append("?" + (i + 1))
          if (i < lessonIds.size - 1) {
            sb2.append(",")
          } else {
            sb2.append(") ")
          }
        }
        entityDao.executeUpdate(sb.toString, lessonIds.toArray())
        entityDao.executeUpdate(sb2.toString, lessonIds.toArray())
      }
    } catch {
      case e: Exception => logger.info("Failure to CourseTake Init " + Throwables.getStackTrace(e))
    }
  }

  private def assignStds(lesson: Lesson, `type`: AssignStdType): Int = {
    var result = 0
    val adminclasses = courseLimitService.extractAdminclasses(lesson.getTeachClass)
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    builder.where("courseTake.lesson=:lesson", lesson)
    if (!adminclasses.isEmpty) {
      builder.where("courseTake.std.adminclass in (:adminclasses)", adminclasses)
    }
    val stdBuilder = OqlBuilder.from(classOf[CourseTake].getName + " courseTake")
    stdBuilder.where("courseTake.lesson.course.code=:courseCode", lesson.getCourse.getCode)
    stdBuilder.where("courseTake.lesson!=:lesson", lesson)
      .select("courseTake.std")
    stdBuilder.where("courseTake.lesson.semester=:semester", lesson.getSemester)
    if (!adminclasses.isEmpty) {
      stdBuilder.where("courseTake.std.adminclass in (:adminclasses)", adminclasses)
    }
    val stds = CollectUtils.newHashSet(entityDao.search(stdBuilder))
    val courseTakes = entityDao.search(builder)
    val removeEntities = CollectUtils.newArrayList()
    val otherCourseTakeTypeCourseTakes = CollectUtils.newHashMap()
    for (courseTake <- courseTakes) {
      if (courseTake.getCourseTakeType.getId == CourseTakeType.NORMAL) {
        removeEntities.add(courseTake)
      } else {
        otherCourseTakeTypeCourseTakes.put(courseTake.getStd, courseTake)
      }
    }
    val saveEntities = CollectUtils.newArrayList()
    val date = new Date()
    val normalCourseTakeType = Model.newInstance(classOf[CourseTakeType], CourseTakeType.NORMAL)
    val assignedMode = Model.newInstance(classOf[ElectionMode], ElectionMode.ASSIGEND)
    for (adminclass <- adminclasses; std <- adminclass.getStudents) {
      val isEven = std.getCode.charAt(std.getCode.length - 1).toInt % 2 == 
        0
      if (`type` == AssignStdType.EVEN && !isEven || (`type` == AssignStdType.ODD && isEven)) {
        //continue
      }
      if (studentService.isActive(std) && studentService.isInschool(std)) {
        if (stds.contains(std)) {
          //continue
        }
        val courseTake = genCourseTake(lesson, std, normalCourseTakeType, assignedMode, date)
        val limitGroup = CourseLimitGroupHelper.getMatchCourseLimitGroup(lesson, std)
        if (limitGroup != null) {
          courseTake.setLimitGroup(limitGroup)
        }
        val logger = electLoggerService.genLogger(courseTake, ElectRuleType.ELECTION, null, date)
        result += 1
        saveEntities.add(courseTake)
        saveEntities.add(logger)
        logger.setCreatedAt(new Date())
        logger.setUpdatedAt(logger.getCreatedAt)
      }
    }
    if (!removeEntities.isEmpty) {
      entityDao.remove(removeEntities)
    }
    entityDao.saveOrUpdate(saveEntities)
    result
  }

  def setStudentService(studentService: StudentService) {
    this.studentService = studentService
  }

  def setCourseLimitService(courseLimitService: CourseLimitService) {
    this.courseLimitService = courseLimitService
  }

  def setElectionDao(electionDao: ElectionDao) {
    this.electionDao = electionDao
  }

  def getCourseTakesByAdminclass(semester: Semester, 
      weekCondition: Condition, 
      project: Project, 
      adminclasses: Adminclass*): List[CourseTake] = {
    getCourseTakesByAdminclass(semester, weekCondition, project, Arrays.asList(adminclasses:_*))
  }

  def getCourseTakesByAdminclass(semester: Semester, 
      weekCondition: Condition, 
      project: Project, 
      adminclasses: Collection[Adminclass]): List[CourseTake] = {
    if (CollectUtils.isNotEmpty(adminclasses)) {
      val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
      builder.where("courseTake.std.adminclass in(:adminclasses)", adminclasses)
      builder.where("courseTake.lesson.project=:project", project)
      if (null != weekCondition) {
        builder.where(weekCondition)
      }
      return entityDao.search(builder)
    }
    Collections.emptyList()
  }

  def getCourseTakesByAdminclassId(semester: Semester, 
      weekCondition: Condition, 
      project: Project, 
      adminclassIds: java.lang.Integer*): List[CourseTake] = {
    if (adminclassIds.length > 0) {
      val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
      builder.where("courseTake.std.adminclass.id in(:adminclassIds)", adminclassIds)
      builder.where("courseTake.lesson.project=:project", project)
      if (null != weekCondition) {
        builder.where(weekCondition)
      }
      return entityDao.search(builder)
    }
    Collections.emptyList()
  }

  def getCourseTakesByTeacher(teacher: Teacher, 
      semester: Semester, 
      weekCondition: Condition, 
      project: Project): List[CourseTake] = {
    val adminclasses = entityDao.search(adminclassSearchHelper.buildQuery(teacher))
    getCourseTakesByAdminclass(semester, weekCondition, project, adminclasses)
  }

  def stateGender(project: Project, semester: Semester, ids: java.lang.Long*): List[CourseTakeStat[String]] = {
    stateGender(project, CollectUtils.newArrayList(semester), ids)
  }

  def stateGender(project: Project, semester: List[Semester], ids: java.lang.Long*): List[CourseTakeStat[String]] = {
    val genders = baseCodeService.getCodes(classOf[Gender])
    stateGender(project, genders, semester, ids)
  }

  def stateGender(project: Project, 
      genders: List[Gender], 
      semesters: List[Semester], 
      ids: java.lang.Long*): List[CourseTakeStat[String]] = {
    var result = CollectUtils.newArrayList()
    if (ids.length > 0) {
      var i = 0
      val maxLength = 498 - genders.size - semesters.size
      while (ids.length > maxLength) {
        var end = i + maxLength
        if (end > ids.length) {
          end = ids.length
        }
        val builder = OqlBuilder.from(classOf[CourseTake].getName + " courseTake")
        builder.where("courseTake.lesson.semester in(:semester)", semesters)
        builder.where("courseTake.lesson.project=:project", project)
        builder.where("courseTake.gender in(:genders)", genders)
        builder.select("new " + classOf[CourseTakeStat].getName + 
          "(courseTake.lesson.id,count(courseTake.std.gender.id),courseTake.std.gender.name)")
        builder.groupBy("courseTake.lesson.id,courseTake.std.gender.id,courseTake.std.gender.name")
        builder.where("courseTake.lesson.id in(:lessonIds)", ArrayUtils.subarray(ids, i, end))
        builder.orderBy("courseTake.lesson.id")
        result.addAll(entityDao.search(builder))
        i += 1
      }
    } else {
      val builder = OqlBuilder.from(classOf[CourseTake].getName + " courseTake")
      builder.where("courseTake.lesson.semester in(:semester)", semesters)
      builder.where("courseTake.lesson.project=:project", project)
      builder.where("courseTake.std.gender in(:genders)", genders)
      builder.select("new " + classOf[CourseTakeStat].getName + 
        "(courseTake.lesson.id,count(courseTake.std.gender.id),courseTake.std.gender.name)")
      builder.groupBy("courseTake.lesson.id,courseTake.std.gender.id,courseTake.std.gender.name")
      builder.orderBy("courseTake.lesson.id")
      result = entityDao.search(builder)
    }
    result
  }

  def setAdminclassSearchHelper(adminclassSearchHelper: AdminclassSearchHelper) {
    this.adminclassSearchHelper = adminclassSearchHelper
  }

  def setBaseCodeService(baseCodeService: BaseCodeService) {
    this.baseCodeService = baseCodeService
  }

  def setCourseTakeFilterStrategy(courseTakeFilterStrategy: CourseTakeFilterStrategy) {
    this.courseTakeFilterStrategy = courseTakeFilterStrategy
  }
}
