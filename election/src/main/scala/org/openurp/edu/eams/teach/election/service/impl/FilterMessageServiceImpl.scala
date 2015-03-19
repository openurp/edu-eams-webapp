package org.openurp.edu.eams.teach.election.service.impl

import java.text.SimpleDateFormat

import java.util.Date



.Entry
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.tuple.Pair
import org.beangle.security.blueprint.User
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.system.msg.service.SystemMessageService
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.FilterMessageService
import org.openurp.edu.teach.lesson.Lesson



class FilterMessageServiceImpl extends BaseServiceImpl with FilterMessageService {

  private var systemMessageService: SystemMessageService = _

  private def searchLogs(project: Project, 
      semester: Semester, 
      startAt: Date, 
      endAt: Date, 
      lessonIds: Array[Long]): List[ElectLogger] = {
    val logQuery = OqlBuilder.from(classOf[ElectLogger], "log")
    logQuery.where("log.createdAt between :start and :end", startAt, endAt)
    logQuery.where("log.semester = :semester", semester)
    logQuery.where("log.project  = :project", project)
    logQuery.where("log.electionMode.id = :modeId", ElectionMode.ASSIGEND)
    if (lessonIds.length > 0) {
      logQuery.where("log.lessonNo in (select l.no from org.openurp.edu.teach.lesson.Lesson l where l.id in (:lessonIds))", 
        lessonIds)
    }
    logQuery.where("log.type = :type", ElectRuleType.WITHDRAW)
    logQuery.where("not exists(" + 
      "from org.openurp.edu.eams.teach.election.ElectLogger wr where wr.lessonNo = log.lessonNo and wr.type = :type1 and wr.stdCode = log.stdCode and wr.createdAt > log.createdAt and log.project = :project1" + 
      ")", ElectRuleType.WITHDRAW, project)
    val logs = entityDao.search(logQuery)
    logs
  }

  private def getNo2LessonMap(project: Project, semester: Semester, lessonIds: Array[Long]): Map[String, Lesson] = {
    val no2lessons = new HashMap[String, Lesson]()
    val lessonQuery = OqlBuilder.from(classOf[Lesson], "lesson")
    lessonQuery.where("lesson.project = :project", project)
      .where("lesson.semester = :semester", semester)
    if (lessonIds.length > 0) {
      lessonQuery.where("lesson.id in (:lessonIds)", lessonIds)
    }
    val lessons = entityDao.search(lessonQuery)
    for (lesson <- lessons) {
      no2lessons.put(lesson.getNo, lesson)
    }
    no2lessons
  }

  private def makeContent(stdCode: String, 
      stdName: String, 
      profile: ElectionProfile, 
      lessons: List[Lesson]): String = {
    val content = new StringBuilder()
    content.append(stdName).append("同学,您好：\n").append("您在 ")
      .append(profile.getName)
      .append(" 选课批次(")
      .append("第")
      .append(profile.getTurn)
      .append("轮")
      .append(")选的以下课程被退课：\n")
    for (lesson <- lessons) {
      content.append("课程序号：").append(lesson.getNo).append(' ')
        .append("课程代码：")
        .append(lesson.getCourse.getCode)
        .append(' ')
        .append("课程名称：")
        .append(lesson.getCourse.getName)
        .append(' ')
      content.append("授课教师：")
      for (teacher <- lesson.getTeachers) {
        content.append(teacher.getName).append(' ')
      }
      content.append('\n')
    }
    content.toString
  }

  private def makeContent(stdCode: String, 
      stdName: String, 
      startAt: Date, 
      endAt: Date, 
      lessons: List[Lesson]): String = {
    val content = new StringBuilder()
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm")
    content.append(stdName).append("同学,您好：\n").append("您在 ")
      .append(sdf.format(startAt))
      .append("~")
      .append(sdf.format(endAt))
      .append("选的以下课程被退课：\n")
    for (lesson <- lessons) {
      content.append("课程序号：").append(lesson.getNo).append(' ')
        .append("课程代码：")
        .append(lesson.getCourse.getCode)
        .append(' ')
        .append("课程名称：")
        .append(lesson.getCourse.getName)
        .append(' ')
      content.append("授课教师：")
      for (teacher <- lesson.getTeachers) {
        content.append(teacher.getName).append(' ')
      }
      content.append('\n')
    }
    content.toString
  }

  def sendWithdrawMessage(project: Project, 
      semester: Semester, 
      profile: ElectionProfile, 
      lessonIds: Array[Long], 
      sender: User) {
    val logs = searchLogs(project, semester, profile.getBeginAt, profile.getEndAt, lessonIds)
    val no2lessons = getNo2LessonMap(project, semester, lessonIds)
    val std2lessons = CollectUtils.newHashMap()
    for (log <- logs) {
      val std = new Pair[String, String](log.getStdCode, log.getStdName)
      var lessons = std2lessons.get(std)
      if (lessons == null) {
        lessons = new ArrayList[Lesson]()
        std2lessons.put(std, lessons)
      }
      val lesson = no2lessons.get(log.getLessonNo)
      if (lesson != null) {
        lessons.add(lesson)
      }
    }
    for ((key, value) <- std2lessons) {
      systemMessageService.sendSimpleMessage("退课通知", makeContent(key.getLeft, key.getRight, profile, 
        value), sender.getName, key.getRight)
    }
  }

  def sendWithdrawMessage(project: Project, 
      semester: Semester, 
      startAt: Date, 
      endAt: Date, 
      lessonIds: Array[Long], 
      sender: User) {
    val logs = searchLogs(project, semester, startAt, endAt, lessonIds)
    val no2lessons = getNo2LessonMap(project, semester, lessonIds)
    val std2lessons = CollectUtils.newHashMap()
    for (log <- logs) {
      val std = new Pair[String, String](log.getStdCode, log.getStdName)
      var lessons = std2lessons.get(std)
      if (lessons == null) {
        lessons = new ArrayList[Lesson]()
        std2lessons.put(std, lessons)
      }
      val lesson = no2lessons.get(log.getLessonNo)
      if (lesson != null) {
        lessons.add(lesson)
      }
    }
    for ((key, value) <- std2lessons) {
      systemMessageService.sendSimpleMessage("退课通知", makeContent(key.getLeft, key.getRight, startAt, 
        endAt, value), sender.getName, key.getLeft)
    }
  }

  def setSystemMessageService(systemMessageService: SystemMessageService) {
    this.systemMessageService = systemMessageService
  }
}
