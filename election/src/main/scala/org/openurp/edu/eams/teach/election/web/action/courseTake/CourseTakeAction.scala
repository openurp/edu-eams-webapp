package org.openurp.edu.eams.teach.election.web.action.courseTake

import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL

import java.util.Arraysimport java.util.Comparator
import java.util.Date



import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.lang3.ArrayUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.Message
import org.beangle.commons.transfer.importer.listener.ItemImporterListener
import org.beangle.commons.web.util.RequestUtils
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.service.UserService
import org.beangle.struts2.helper.QueryHelper
import org.openurp.base.Semester
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.base.Student
import org.openurp.edu.eams.system.msg.service.SystemMessageService
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.code.school.CourseHourType
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.dao.ElectionDao
import org.openurp.edu.eams.teach.election.model.ElectMailTemplate
import org.openurp.edu.eams.teach.election.service.CourseTakeService
import org.openurp.edu.eams.teach.election.service.ElectionProfileService
import org.openurp.edu.eams.teach.election.service.context.ConflictStatWrapper
import org.openurp.edu.eams.teach.election.service.event.ElectCourseEvent
import org.openurp.edu.eams.teach.election.service.helper.FreeMarkerHelper
import org.openurp.edu.eams.teach.election.service.impl.CourseTakeImportListener
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.util.DownloadHelper
import com.opensymphony.xwork2.util.ClassLoaderUtil



class CourseTakeAction extends CourseTakeSearchAction {

   var courseTakeService: CourseTakeService = _

   var userService: UserService = _

   var electionDao: ElectionDao = _

   var electionProfileService: ElectionProfileService = _

   var systemMessageService: SystemMessageService = _

  def search(): String = super.search()

  def editCourseTakeType(): String = {
    val ids = Strings.transformToLong(Strings.split(get("courseTakeIds"), ","))
    var update = 0
    if (ArrayUtils.isNotEmpty(ids)) {
      val courseTakeType = getEntity(classOf[CourseTakeType], "courseTakeType")
      try {
        val parameterMap = Collections.newMap[Any]
        parameterMap.put("courseTakeType", courseTakeType)
        parameterMap.put("ids", ids)
        parameterMap.put("updatedAt", new Date())
        update = entityDao.executeUpdate("update " + classOf[CourseTake].getName + " courseTake " + 
          "set courseTake.courseTakeType=:courseTakeType,updatedAt=:updatedAt " + 
          "where courseTake.id in(:ids)", parameterMap)
        if (update > 0) {
          val courseTakes = entityDao.get(classOf[CourseTake], ids)
          for (courseTake <- courseTakes) {
            courseTakeService.publish(ElectCourseEvent.create(courseTake))
          }
          return redirect("search", "保存成功 " + update + " 条记录")
        }
      } catch {
        case e1: Exception => 
      }
    }
    redirect("search", "info.save.failure")
  }

  override def importForm(): String = forward()

  protected def getImporterListeners(): List[ItemImporterListener] = {
    val listeners = Collections.newBuffer[Any]
    listeners.add(new CourseTakeImportListener(electionDao, putSemester(null), getProject))
    listeners
  }

  def taskList(): String = {
    val builder = OqlBuilder.from(classOf[Lesson], "task")
    QueryHelper.populateConditions(builder)
    builder.where("task.teachClass.stdType in(:stdTypes)", getStdTypes)
    builder.where("task.teachDepart in(:departs)", getDeparts)
    builder.limit(getPageLimit)
    put("tasks", entityDao.search(builder))
    forward()
  }

  def addLessonsForStds(): String = {
    val stdIds = getLongIds("std")
    if (ArrayUtils.isNotEmpty(stdIds)) {
      val stds = entityDao.get(classOf[Student], stdIds)
      val stdCodes = new StringBuilder()
      for (std <- stds) {
        stdCodes.append(std.getCode).append(',')
      }
      put("stdCodes", stdCodes.toString)
    }
    forward()
  }

  def stdList(): String = {
    val query = OqlBuilder.from(classOf[Student], "student")
    populateConditions(query, "student.id")
    query.where("student.project=:project", getProject)
    var stdCodeSeq = get("fake.codes")
    if (Strings.isNotEmpty(stdCodeSeq)) {
      stdCodeSeq = stdCodeSeq.replaceAll("，+", ",").replaceAll("\\s+", ",")
        .replaceAll(",+", ",")
      val stdCodes = Strings.split(stdCodeSeq)
      if (stdCodes.length == 1) {
        query.where("student.code like :stdCode", '%' + stdCodes(0) + '%')
      } else {
        query.where("student.code in (:stdCodes)", stdCodes)
      }
    }
    if (Strings.isNotEmpty(get(Order.ORDER_STR))) {
      query.orderBy(get(Order.ORDER_STR))
    }
    val limit = getPageLimit
    query.limit(limit)
    put("students", entityDao.search(query))
    forward("addLessonsForStds/stdList")
  }

  def lessonList(): String = {
    var stdCodes: Array[String] = null
    var stdCodeSeq = get("stdCodes")
    if (Strings.isNotEmpty(stdCodeSeq)) {
      stdCodeSeq = stdCodeSeq.replaceAll("，+", ",").replaceAll("\\s+", ",")
        .replaceAll(",+", ",")
      stdCodes = Strings.split(stdCodeSeq)
    }
    if (ArrayUtils.isEmpty(stdCodes)) {
      put("lessons", new ArrayList[Lesson]())
      return forward("addLessonsForStds/lessonList")
    }
    val semester = putSemester(null)
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    populateConditions(query, "lesson.id")
    query.where("lesson.project = :project", getProject)
    query.where("lesson.semester = :semester", semester)
    query.where("not exists(from lesson.teachClass.courseTakes take where take.std.code in (:codes))", 
      stdCodes)
    var lessonNoSeq = get("fake.nos")
    if (Strings.isNotEmpty(lessonNoSeq)) {
      lessonNoSeq = lessonNoSeq.replaceAll("，+", ",").replaceAll("\\s+", ",")
        .replaceAll(",+", ",")
      val lessonNoes = Strings.split(lessonNoSeq)
      query.where("lesson.no in (:nos)", lessonNoes)
    }
    if (Strings.isNotEmpty(get(Order.ORDER_STR))) {
      query.orderBy(get(Order.ORDER_STR))
    }
    val limit = getPageLimit
    limit.setPageSize(10)
    query.limit(limit)
    put("lessons", entityDao.search(query))
    forward("addLessonsForStds/lessonList")
  }

  def add(): String = {
    val lessonNos = Strings.split(get("lessonNos"))
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val stdCodes = Strings.split(get("stdCodes"))
    val messages = Collections.newBuffer[Any]
    if ((ArrayUtils.isEmpty(lessonNos) && ArrayUtils.isEmpty(lessonIds)) || 
      ArrayUtils.isEmpty(stdCodes)) {
      put("messages", Collections.newBuffer[Any](new Message("0", Array(Collections.emptyList())), new Message("0", 
        Array(Collections.emptyMap()))))
      return "addResult"
    }
    val students = entityDao.get(classOf[Student], "code", Arrays.asList(stdCodes:_*))
    val lQuery = OqlBuilder.from(classOf[Lesson], "lesson")
    lQuery.where("lesson.project = :project", getProject)
      .where("lesson.semester = :semester", putSemester(null))
    if (ArrayUtils.isEmpty(lessonNos)) {
      lQuery.where("lesson.id in (:ids)", lessonIds)
    } else {
      lQuery.where("lesson.no in (:nos)", lessonNos)
    }
    val lessons = entityDao.search(lQuery)
    val unCheckTimeConflict = true == getBoolean("unCheckTimeConflict")
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    builder.where("courseTake.lesson.semester = :semester", putSemester(null))
    builder.where("courseTake.std.code in (:stdCodes)", stdCodes)
    for (student <- students) {
      val existedTakes = entityDao.search(builder)
      messages.addAll(courseTakeService.election(student, existedTakes, lessons, unCheckTimeConflict))
    }
    put("messages", messages)
    "addResult"
  }

  def withdraw(): String = {
    val ids = Strings.transformToLong(Strings.split(get("courseTakeIds"), ","))
    var messages = Collections.newBuffer[Any]
    if (ArrayUtils.isNotEmpty(ids)) {
      val courseTakes = entityDao.get(classOf[CourseTake], ids)
      val sendMessage = getBool("sendMessage")
      var sender: User = null
      if (sendMessage) {
        val userName = getUsername
        sender = userService.get(userName)
      }
      try {
        messages = courseTakeService.withdraw(courseTakes, sender)
      } catch {
        case e: InterruptedException => 
      }
    } else {
      messages.add(new Message("没有需要退课的操作"))
    }
    put("messages", messages)
    "sendMessage"
  }

  def editMessage(): String = {
    val ids = Strings.transformToLong(Strings.split(get("courseTakeIds"), ","))
    var template: ElectMailTemplate = null
    if (ArrayUtils.isNotEmpty(ids)) {
      val courseTakes = entityDao.get(classOf[CourseTake], ids)
      val stdMessageContents = Collections.newMap[Any]
      val templateId = getLong("electMailTemplate.id")
      if (null != templateId) {
        template = entityDao.get(classOf[ElectMailTemplate], templateId)
      }
      for (courseTake <- courseTakes) {
        if (null != template) {
          stdMessageContents.put(courseTake, FreeMarkerHelper.dynamicCompileTemplate(template, courseTake))
        } else {
          stdMessageContents.put(courseTake, null)
        }
      }
      put("stdMessageContents", stdMessageContents)
      put("user", getUser)
    }
    put("editTemplate", null == template)
    forward()
  }

  def sendMessage(): String = {
    var receiptorName: String = null
    val username = getUsername
    val messages = Collections.newBuffer[Any]
    var i = 0
    while (null != (receiptorName = get("std" + i + "Code"))) {
      val text = get("std" + i + "ContentText")
      val subject = get("std" + i + "Subject")
      val name = get("std" + i + "Name")
      val lessonInfo = get("courseTake" + i + "LessonInfo")
      val sb = new StringBuilder(name).append(name).append("(").append(receiptorName)
        .append(")")
        .append(lessonInfo)
      if (systemMessageService.sendSimpleMessage(subject, text, username, receiptorName)) {
        sb.append("消息发送成功")
      } else {
        sb.append("消息发送失败")
      }
      messages.add(new Message(sb.toString))
      i += 1
    }
    if (messages.isEmpty) {
      messages.add((new Message("没有需要发送的邮件")))
    }
    put("messages", messages)
    forward()
  }

  def electWithdrawInfo(): String = {
    val ids = Strings.transformToLong(Strings.split(get("courseTakeIds"), ","))
    if (ArrayUtils.isNotEmpty(ids)) {
      val semester = putSemester(null)
      val builder = OqlBuilder.from(classOf[CourseTake].getName + " courseTake")
      builder.where("courseTake.id in(:ids)", ids).select("courseTake.std.code")
      val stdCodes = entityDao.search(builder)
      val electBuilder = OqlBuilder.from(classOf[ElectLogger], "electLogger")
      electBuilder.where("electLogger.stdCode in(:stdCodes) and electLogger.semester=:semester", stdCodes, 
        semester)
      electBuilder.orderBy("electLogger.updatedAt")
      put("loggers", entityDao.search(electBuilder))
    } else {
      put("loggers", Collections.newBuffer[Any])
    }
    forward()
  }

  def collisionStdList(): String = {
    val semester = putSemester(null)
    val builder = OqlBuilder.from(classOf[CourseTake].getName + " courseTake," + classOf[CourseTake].getName + 
      " courseTake2")
    builder.select("distinct courseTake.id,courseTake2.id")
      .join("courseTake.lesson.schedule.activities", "courseActivity")
      .join("courseTake2.lesson.schedule.activities", "courseActivity2")
      .where("courseTake.std=courseTake2.std")
      .where("courseTake.lesson.semester=courseTake2.lesson.semester")
      .where("courseTake.lesson.semester=:semestrer", semester)
      .where("BITAND(courseActivity.time.state,courseActivity2.time.state)>0")
      .where("courseActivity.time.day=courseActivity2.time.day")
      .where("courseActivity.time.start <= courseActivity2.time.end ")
      .where("courseActivity.time.end >= courseActivity2.time.start ")
      .where("courseTake.id <> courseTake2.id")
    val rows = entityDao.search(builder)
    val courseTakeIds = Collections.newSet[Any]
    val wrapperMap = Collections.newMap[Any]
    for (row <- rows) {
      val id = row(0).asInstanceOf[java.lang.Long]
      val conflictId = row(1).asInstanceOf[java.lang.Long]
      courseTakeIds.add(id)
      courseTakeIds.add(conflictId)
      var wrapper = wrapperMap.get(id)
      if (null == wrapper) {
        wrapper = new ConflictStatWrapper(id)
        wrapperMap.put(id, wrapper)
      }
      wrapper.getConflicts.add(conflictId)
    }
    val takes = entityDao.get(classOf[CourseTake], courseTakeIds)
    val courseTakes = Collections.newMap[Any]
    for (courseTake <- takes) {
      courseTakes.put(courseTake.id, courseTake)
    }
    put("courseTakes", courseTakes)
    put("wrappers", wrapperMap.values)
    forward()
  }

  def downloadTemplate(): String = {
    val template = get("template")
    val url = ClassLoaderUtil.getResource(template, this.getClass)
    val fileName = url.getFile
    val workbook = new HSSFWorkbook(url.openStream())
    val sheet = workbook.getSheetAt(0)
    val titles = sheet.getRow(0)
    val keys = sheet.getRow(1)
    val courseHourTypes = baseCodeService.getCodes(classOf[CourseHourType])
    for (courseHourType <- courseHourTypes) {
      val hourTitleCell = titles.createCell(titles.getLastCellNum, Cell.CELL_TYPE_STRING)
      hourTitleCell.setCellValue(courseHourType.getName)
      val hourKeyCell = keys.createCell(keys.getLastCellNum, Cell.CELL_TYPE_STRING)
      hourKeyCell.setCellValue("period_hours_" + courseHourType.getCode)
    }
    val response = getResponse
    val request = getRequest
    response.reset()
    var contentType = response.getContentType
    val attch_name = DownloadHelper.getAttachName(fileName)
    if (null == contentType) {
      contentType = "application/x-msdownload"
      response.setContentType(contentType)
      logger.debug("set content type {} for {}", contentType, attch_name)
    }
    response.addHeader("Content-Disposition", "attachment; filename=\"" + RequestUtils.encodeAttachName(request, 
      "上课名单导入模板") + 
      "\"")
    workbook.write(response.getOutputStream)
    null
  }

  def searchAssignConflict(): String = {
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    builder.where("courseTake.lesson.semester=:semester", putSemester(null))
    builder.where("courseTake.electionMode.id=:electionModeId", ElectionMode.ASSIGEND)
    val courseTakes = entityDao.search(builder)
    val courseTakeMap = Collections.newMap[Any]
    for (courseTake <- courseTakes) {
      var takes = courseTakeMap.get(courseTake.getStd)
      if (null == takes) {
        takes = Collections.newBuffer[Any]
        courseTakeMap.put(courseTake.getStd, takes)
      }
      takes.add(courseTake)
    }
    val conflictMap = Collections.newMap[Any]
    for (takes <- courseTakeMap.values; courseTake <- takes; courseTake2 <- takes if isConflict(courseTake.getLesson, 
      courseTake2.getLesson)) {
      var conflicts = conflictMap.get(courseTake.getStd)
      if (null == conflicts) {
        conflicts = Collections.newSet[Any]
        conflictMap.put(courseTake.getStd, conflicts)
      }
      val lessons = Collections.newBuffer[Any](courseTake.getLesson, courseTake2.getLesson)
      Collections.sort(lessons, new Comparator[Lesson]() {

        def compare(o1: Lesson, o2: Lesson): Int = return (o1.id - o2.id).toInt
      })
      val lessonStr = lessons.get(0).getCourse.getName + "[" + lessons.get(0).getNo + 
        "]"
      val lesson1Str = lessons.get(1).getCourse.getName + "[" + lessons.get(1).getNo + 
        "]"
      conflicts.add(lessonStr + "与" + lesson1Str + "冲突")
    }
    put("conflictMap", conflictMap)
    forward()
  }

  def searchLessonConflict(): String = {
    val semester = putSemester(null)
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.semester=:semester", semester)
    val lessons = entityDao.search(builder)
    val conflicts = Collections.newMap[Any]
    for (lesson1 <- lessons; lesson2 <- lessons if lesson1 != lesson2 && 
      lesson1.getCourse.getCode != lesson2.getCourse.getCode) {
      val msgs = getConflict(lesson1, lesson2)
      if (!msgs.isEmpty) {
        var conflictLessons = conflicts.get(lesson1)
        if (null == conflictLessons) {
          conflictLessons = Collections.newMap[Any]
          conflicts.put(lesson1, conflictLessons)
        }
        conflictLessons.put(lesson2, msgs)
      }
    }
    put("conflicts", conflicts)
    put("lessons", conflicts.keySet)
    forward()
  }

  private def getConflict(lesson: Lesson, lesson2: Lesson): List[String] = {
    val activities = lesson.getCourseSchedule.getActivities
    val activities2 = lesson2.getCourseSchedule.getActivities
    val result = Collections.newBuffer[Any]
    for (courseActivity <- activities) {
      val time = courseActivity.getTime
      for (courseActivity2 <- activities2) {
        val time2 = courseActivity2.getTime
        if ((time.state & time2.state) > 0 && time.day == time2.day && 
          time.getStartUnit <= time2.getEndUnit && 
          time.getEndUnit >= time2.getStartUnit) {
          result.add(WeekDays.get(time2.day).getName + "第" + time2.getStartUnit + 
            "-" + 
            time2.getEndUnit)
        }
      }
    }
    result
  }

  private def isConflict(lesson: Lesson, lesson2: Lesson): Boolean = {
    if (null == lesson || null == lesson2 || lesson == lesson2 || 
      lesson.getCourse.getCode == lesson2.getCourse.getCode) return false
    val activities = lesson.getCourseSchedule.getActivities
    val activities2 = lesson2.getCourseSchedule.getActivities
    for (courseActivity <- activities) {
      val time = courseActivity.getTime
      for (courseActivity2 <- activities2) {
        val time2 = courseActivity2.getTime
        if ((time.state & time2.state) > 0 && time.day == time2.day && 
          time.getStartUnit <= time2.getEndUnit && 
          time.getEndUnit >= time2.getStartUnit) {
          return true
        }
      }
    }
    false
  }

  def reInitDatas(): String = {
    val builder = OqlBuilder.from(classOf[ElectionProfile].getName + " profile")
    builder.where("profile.semester=:semester", putSemester(null))
    builder.select("profile.id")
    val profileIds = entityDao.search(builder)
    for (profileId <- profileIds) {
      electionProfileService.initDataByChance(profileId)
    }
    null
  }
}
