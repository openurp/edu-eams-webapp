package org.openurp.edu.eams.teach.election.web.action.courseTake

import java.io.IOException
import java.io.PrintWriter
import java.util.Calendarimport java.util.Date




import javax.servlet.http.HttpServletResponse
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.time.DateUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.entity.util.EntityUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import org.beangle.commons.text.i18n.Message
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.service.UserService
import org.beangle.struts2.convention.route.Action
import org.openurp.base.Campus
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.code.person.Gender
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.eams.classroom.Occupancy
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator.Usage
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.Enum.AssignStdType
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.CourseTakeService
import org.openurp.edu.eams.teach.election.service.ElectionProfileService
import org.openurp.edu.eams.teach.election.service.FilterMessageService
import org.openurp.edu.eams.teach.election.service.context.CourseTakeStat
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonMaterial
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.dao.LessonPlanRelationDao
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import org.openurp.edu.eams.web.helper.StdSearchHelper
import org.openurp.edu.eams.web.util.OutputProcessObserver
import org.openurp.edu.eams.web.util.OutputWebObserver



class CourseTakeForTaskAction extends SemesterSupportAction {

  var lessonService: LessonService = _

  var courseTakeService: CourseTakeService = _

  var lessonSearchHelper: LessonSearchHelper = _

  var stdSearchHelper: StdSearchHelper = _

  var userService: UserService = _

  var courseLimitService: CourseLimitService = _

  var electionProfileService: ElectionProfileService = _

  var lessonDao: LessonDao = _

  var lessonPlanRelationService: LessonPlanRelationService = _

  var lessonLogHelper: LessonLogHelper = _

  var lessonPlanRelationDao: LessonPlanRelationDao = _

  var filterMessageService: FilterMessageService = _

  def index(): String = {
    val semester = putSemester(null)
    val departments = getDeparts
    if (CollectUtils.isEmpty(departments)) {
      return forwardError("您没有操作权限")
    }
    val teachDepartBuilder = OqlBuilder.from(classOf[Lesson].getName, "lesson")
    teachDepartBuilder.where("lesson.project =:project", getProject)
    teachDepartBuilder.where("lesson.semester =:semester", semester)
    teachDepartBuilder.where("lesson.teachDepart in (:departments)", departments)
    teachDepartBuilder.select("distinct lesson.teachDepart")
    val courseTypeBuilder = OqlBuilder.from(classOf[Lesson].getName, "lesson")
    courseTypeBuilder.where("lesson.project =:project", getProject)
    courseTypeBuilder.where("lesson.semester =:semester", semester)
    courseTypeBuilder.select("distinct lesson.courseType")
    put("teachDeparts", entityDao.search(teachDepartBuilder))
    put("teachClassDeparts", departments)
    put("courseTypes", entityDao.search(courseTypeBuilder))
    addBaseInfo("campuses", classOf[Campus])
    put("stdTypes", getStdTypes)
    put("weeks", WeekDays.All)
    put("lessonAuditStates", CommonAuditState.values)
    forward()
  }

  def taskList(): String = {
    val departments = getDeparts
    if (CollectUtils.isEmpty(departments)) {
      return forwardError("您没有操作权限")
    }
    val builder = lessonSearchHelper.buildQuery()
    builder.where("lesson.teachDepart in (:department)", departments)
    builder.where("lesson.semester = :semester", putSemester(null))
    val isElectable = getBoolean("isElectable")
    if (null != isElectable) {
      builder.where((if (isElectable) "" else "not ") + "exists (from " + 
        classOf[ElectionProfile].getName + 
        " electionProfile join electionProfile.electableLessons electableLesson " + 
        "where electableLesson.id=lesson.id and electionProfile.semester=lesson.semester)")
    }
    val lessons = entityDao.search(builder)
    val digestor = CourseActivityDigestor.getInstance.setDelimeter("<br>")
    val arrangeInfo = new HashMap[String, String]()
    for (oneTask <- lessons) {
      arrangeInfo.put(oneTask.id.toString, digestor.digest(getTextResource, oneTask))
    }
    put("arrangeInfo", arrangeInfo)
    put("lessons", lessons)
    put("guaPaiTag", Model.newInstance(classOf[LessonTag], LessonTag.PredefinedTags.GUAPAI.id))
    forward()
  }

  def adminClassStdCount(): String = {
    val lessonIds = getLongIds("lesson")
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    val adminClassStdMap = CollectUtils.newHashMap()
    for (lesson <- lessons) {
      adminClassStdMap.put(lesson, new HashMap[Adminclass, Long]())
      val count = adminClassStdMap.get(lesson)
      for (courseTake <- lesson.getTeachClass.getCourseTakes) {
        if (null == count.get(courseTake.getStd.getAdminclass)) {
          count.put(courseTake.getStd.getAdminclass, 1l)
        } else {
          var c = count.get(courseTake.getStd.getAdminclass)
          count.put(courseTake.getStd.getAdminclass, c)
        }
      }
    }
    put("lessons", lessons)
    put("adminClassStdMap", adminClassStdMap)
    forward()
  }

  def showFilterDialog(): String = {
    val semester = putSemester(null)
    put("semesterId", semester)
    put("lessonIds", get("lessonIds"))
    val profiles = electionProfileService.getProfileBySemester(semester, getProject)
    put("profiles", profiles)
    forward()
  }

  def filterCourseTakes(): String = {
    if (CollectUtils.isEmpty(getDeparts)) {
      return forwardError("您没有操作权限")
    }
    val successCount = CollectUtils.newHashMap()
    val allThisTurnSelfTakes = getMaybeFilteredCourseTakes
    val lesson2takes = CollectUtils.newHashMap()
    for (courseTake <- allThisTurnSelfTakes) {
      var courseTakes = lesson2takes.get(courseTake.getLesson)
      if (null == courseTakes) {
        courseTakes = CollectUtils.newArrayList()
        lesson2takes.put(courseTake.getLesson, courseTakes)
      }
      courseTakes.add(courseTake)
    }
    for (lesson <- lesson2takes.keySet) {
      val amount = lesson.getTeachClass.getStdCount - 
        (lesson.getTeachClass.getLimitCount - lesson.getTeachClass.getReservedCount)
      val selfTakes = lesson2takes.get(lesson)
      try {
        val msgs = courseTakeService.filter(amount, selfTakes, getFilterParams)
        for (message <- msgs if true == message.getParams.iterator().next()) {
          successCount.put(lesson, (if (successCount.get(lesson) == null) 1 else successCount.get(lesson) + 1))
        }
      } catch {
        case e: Exception => {
          logger.error(Throwables.getStackTrace(e))
          successCount.put(lesson, 0)
        }
      }
    }
    val sb = new StringBuilder()
    for (lesson <- successCount.keySet) {
      sb.replace(0, sb.length, "")
      sb.append("update ").append(classOf[Lesson].getName)
        .append(" lesson \n")
        .append("set lesson.teachClass.stdCount = ( \n")
        .append("select count(take.id) from ")
        .append(classOf[CourseTake].getName)
        .append(" take \n")
        .append("where take.lesson.id=lesson.id \n")
        .append(")\n")
        .append("where lesson.id = ?1")
      entityDao.executeUpdate(sb.toString, lesson.id)
    }
    put("successCount", successCount)
    forward()
  }

  protected def getMaybeFilteredCourseTakes(): List[CourseTake] = {
    val departs = getDeparts
    val lessonIds = getLongIds("lesson")
    val startAt = DateUtils.truncate(getDateTime("startAt"), Calendar.MINUTE)
    val endAt = DateUtils.truncate(getDateTime("endAt"), Calendar.MINUTE)
    val allThisTurnSelfTakeQuery = OqlBuilder.from(classOf[CourseTake], "courseTake")
    if (ArrayUtils.isNotEmpty(lessonIds)) {
      allThisTurnSelfTakeQuery.where("courseTake.lesson.id in(:lessonIds)", lessonIds)
    }
    allThisTurnSelfTakeQuery.where("courseTake.updatedAt >= :startAt and courseTake.updatedAt <=:endAt", 
      startAt, endAt)
      .where("courseTake.lesson.teachClass.limitCount < courseTake.lesson.teachClass.stdCount")
      .where("courseTake.lesson.teachDepart in (:departs)", departs)
      .where("courseTake.lesson.project = :projectId", getProject)
      .where("courseTake.lesson.semester = :semetser", putSemester(null))
      .where("courseTake.electionMode.id = :electionModeId", ElectionMode.SELF)
    entityDao.search(allThisTurnSelfTakeQuery)
  }

  protected def getFilterParams(): Map[String, Any] = null

  def showSendFilterMessageDialog(): String = {
    val semester = putSemester(null)
    put("semesterId", semester)
    val profiles = electionProfileService.getProfileBySemester(semester, getProject)
    put("profiles", profiles)
    forward()
  }

  def sendFilterMessage(): String = {
    var profile: ElectionProfile = null
    if (getLongId("profile") != null) {
      profile = entityDao.get(classOf[ElectionProfile], getLongId("profile"))
    }
    val project = getProject
    val semester = putSemester(null)
    val startAt = DateUtils.truncate(getDateTime("startAt"), Calendar.MINUTE)
    val endAt = DateUtils.truncate(getDateTime("endAt"), Calendar.MINUTE)
    val lessonIds = getLongIds("lesson")
    val sender = userService.get(getUserId)
    if (profile == null) {
      filterMessageService.sendWithdrawMessage(project, semester, startAt, endAt, lessonIds, sender)
    } else {
      filterMessageService.sendWithdrawMessage(project, semester, profile, lessonIds, sender)
    }
    redirect("index", "info.send.success")
  }

  def assignStds(): String = {
    if (CollectUtils.isEmpty(getDeparts)) {
      return forwardError("您没有操作权限")
    }
    val lessonIds = getLongIds("lesson")
    if (ArrayUtils.isEmpty(lessonIds)) {
      return forwardError("error.model.id.needed")
    }
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.id in(:lessonIds)", lessonIds)
    builder.where("lesson.teachDepart in(:departs)", getDeparts)
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    if (CollectUtils.isEmpty(lessons)) {
      return forwardError("error.model.id.needed")
    }
    val response = getResponse
    response.setContentType("text/html; charset=utf-8")
    val path = "electProcessDisplay.ftl"
    val observer = new OutputWebObserver(response.getWriter, getTextResource, path)
    info(getUser + " assign course take for taskId[" + EntityUtils.extractIds(lessons) + 
      "]")
    val assignStdType = get("assignStdType")
    var `type`: AssignStdType = null
    `type` = if (Strings.isBlank(assignStdType)) AssignStdType.ALL else AssignStdType.valueOf(assignStdType.toUpperCase())
    courseTakeService.assignStds(lessons, `type`, putSemester(null), observer)
    response.getWriter.flush()
    response.getWriter.close()
    null
  }

  def teachClassStdList(): String = {
    val lessonId = getLongId("lesson")
    if (null != lessonId) {
      put("lesson", entityDao.get(classOf[Lesson], lessonId))
      val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
      populateConditions(builder)
      builder.where("courseTake.lesson.id=:lessonId", lessonId)
        .orderBy(get(Order.ORDER_STR))
        .limit(getPageLimit)
      put("courseTakes", entityDao.search(builder))
    }
    forward()
  }

  def loggerList(): String = {
    val lessonId = getLong("lesson.id")
    if (null == lessonId) return forwardError("error.model.id.needed")
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    if (null == lesson) {
      return forwardError("error.model.id.needed")
    }
    put("lesson", lesson)
    var builder = OqlBuilder.from(classOf[ElectLogger], "logger")
    builder.where("logger.lessonNo=:lessonNo", lesson.getNo)
    populateConditions(builder)
    val `type` = get("type")
    if (Strings.isNotBlank(`type`)) {
      val electRuleType = ElectRuleType.valueOf(`type`.toUpperCase())
      builder.where("logger.type=:type", electRuleType)
      put("electType", `type`)
    }
    val order = get(Order.ORDER_STR)
    if (null != order && order.startsWith(builder.getAlias)) {
      builder = builder.orderBy(get(Order.ORDER_STR))
    }
    put("loggers", entityDao.search(builder.limit(getPageLimit)))
    put("courseTakes", entityDao.get(classOf[CourseTake], "lesson", lesson))
    put("electTypes", ElectRuleType.getElectTypes)
    forward()
  }

  def add(): String = {
    val stdIds = getLongIds("std")
    val lessonId = getLong("lessonId")
    put("lessonId", lessonId)
    if (ArrayUtils.isNotEmpty(stdIds) && null != lessonId) {
      val lesson = entityDao.get(classOf[Lesson], lessonId)
      val students = entityDao.get(classOf[Student], stdIds)
      val unCheckTimeConflict = true == getBoolean("unCheckTimeConflict")
      val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
      builder.where("courseTake.lesson.semester=:semester", lesson.getSemester)
      builder.where("courseTake.std.id in(:stdIds)", stdIds)
      val messages = courseTakeService.election(students, entityDao.search(builder), lesson, unCheckTimeConflict)
      put("messages", messages)
    } else {
      put("messages", CollectUtils.newArrayList(new Message("0", Array(Collections.emptyList())), new Message("0", 
        Array(Collections.emptyMap()))))
    }
    "addResult"
  }

  def withdraw(): String = {
    val courseTakeIds = getLongIds("courseTake")
    val courseTakes = entityDao.get(classOf[CourseTake], courseTakeIds)
    try {
      courseTakeService.withdraw(courseTakes, null)
    } catch {
      case e: InterruptedException => return redirect("teachClassStdList", "info.action.failure", "&lesson.id=" + get("lesson.id"))
    }
    redirect("teachClassStdList", "info.action.success", "&lesson.id=" + get("lesson.id"))
  }

  def stdList(): String = {
    val lessonId = getLong("lessonId")
    val query = stdSearchHelper.buildStdQuery()
    query.where("not exists(from " + classOf[CourseTake].getName + 
      " courseTake where courseTake.std = std and courseTake.lesson.id=:lesson)", lessonId)
    query.where("std.project =:project", getProject)
    put("lessonId", lessonId)
    put("stdList", entityDao.search(query))
    addBaseCode("genders", classOf[Gender])
    forward()
  }

  def statStdCount(): String = {
    val semester = putSemester(null)
    val ids = getLongIds("lesson.id")
    val hqlBuilder = new StringBuilder("update ").append(classOf[Lesson].getName)
      .append(" lesson ")
    hqlBuilder.append("set lesson.teachClass.stdCount=(select count(*) from ")
      .append(classOf[CourseTake].getName)
      .append(" courseTake where courseTake.lesson.id=lesson.id) ")
    hqlBuilder.append("where lesson.semester.id=?1")
    if (ArrayUtils.isNotEmpty(ids)) {
      val hql = hqlBuilder.append("and lesson.id=?2").toString
      for (id <- ids) {
        entityDao.executeUpdate(hql.toString, semester.id, id)
      }
    } else {
      entityDao.executeUpdate(hqlBuilder.toString, semester.id)
    }
    redirect("index", "info.stat.success")
  }

  def batchUpdateStdCountSetting() {
    val response = getResponse
    val write = response.getWriter
    val lessons = CollectUtils.newArrayList()
    for (i <- 0 until 1000) {
      val lesson = populateEntity(classOf[Lesson], "lesson" + i)
      if (lesson.isPersisted) {
        lessons.add(lesson)
      }
    }
    try {
      entityDao.saveOrUpdate(lessons)
      write.write("success")
    } catch {
      case e: Exception => write.write("保存失败")
    } finally {
      if (null != write) {
        try {
          write.flush()
          write.close()
        } catch {
          case e2: Exception => 
        }
      }
    }
  }

  def batchUpdateStdCount(): String = {
    val ids = Strings.transformToLong(get("taskIds").split(","))
    if (ids == null || ids.length == 0) {
      return forward(new Action("", "taskList"), "info.action.failure")
    }
    val tasks = entityDao.get(classOf[Lesson], ids)
    entityDao.saveOrUpdate(tasks)
    redirect(new Action("", "taskList"), "info.action.success")
  }

  def remove(): String = {
    val lessonIds = getLongIds("lesson")
    if (ArrayUtils.isNotEmpty(lessonIds)) {
      val lessons = entityDao.get(classOf[Lesson], lessonIds)
      for (task <- lessons if !getProjects.contains(task.getProject) || !getDeparts.contains(task.getTeachDepart)) {
        return forwardError("error.depart.dataRealm.insufficient")
      }
      for (lesson <- lessons) {
        try {
          val builder = OqlBuilder.from(classOf[Occupancy], "occupancy").where("occupancy.userid in( :lessonIds)", 
            RoomUseridGenerator.gen(lesson, Usage.COURSE, Usage.EXAM))
          val occupancies = entityDao.search(builder)
          entityDao.remove(occupancies)
          lessonPlanRelationDao.removeRelation(lesson)
          entityDao.remove(entityDao.get(classOf[LessonMaterial], "lesson", lesson))
          entityDao.remove(lesson)
          lessonLogHelper.log(LessonLogBuilder.delete(lesson, null))
          return redirect("taskList", "info.delete.success")
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
    redirect("taskList", "info.delete.failure")
  }

  def statTakes(): String = {
    val semester = putSemester(null)
    val project = getProject
    val genders = baseCodeService.getCodes(classOf[Gender])
    val genderCountMap = CollectUtils.newHashMap()
    val courseTakeStats = courseTakeService.stateGender(project, genders, CollectUtils.newArrayList(semester))
    val lessons = entityDao.get(classOf[Lesson], Array("project", "semester"), Array(project, semester))
    val lessonMap = CollectUtils.newHashMap()
    val removeLessonIds = CollectUtils.newHashSet()
    for (lesson <- lessons) {
      lessonMap.put(lesson.id, lesson)
    }
    val stats = CollectUtils.newHashMap()
    for (courseTakeStat <- courseTakeStats) {
      val lessonId = courseTakeStat.id
      val lesson = lessonMap.get(lessonId)
      removeLessonIds.add(lessonId)
      courseTakeStat.setLesson(lesson)
      var lessonStats = stats.get(lesson)
      if (null == lessonStats) {
        lessonStats = CollectUtils.newHashMap()
        stats.put(lesson, lessonStats)
      }
      lessonStats.put(courseTakeStat.getStatBy, courseTakeStat)
      var count = genderCountMap.get(courseTakeStat.getStatBy)
      if (null == count) {
        count = 0L
      }
      genderCountMap.put(courseTakeStat.getStatBy, count += courseTakeStat.getCount)
    }
    for (lesson <- lessonMap.values if !removeLessonIds.contains(lesson.id); gender <- genders) {
      val courseTakeStat = new CourseTakeStat[String](lesson.id, 0, gender.getName)
      courseTakeStat.setLesson(lesson)
      var lessonStats = stats.get(lesson)
      if (null == lessonStats) {
        lessonStats = CollectUtils.newHashMap()
        stats.put(lesson, lessonStats)
      }
      lessonStats.put(courseTakeStat.getStatBy, courseTakeStat)
      var count = genderCountMap.get(courseTakeStat.getStatBy)
      if (null == count) {
        count = 0L
      }
      genderCountMap.put(courseTakeStat.getStatBy, count += courseTakeStat.getCount)
    }
    put("stats", stats)
    put("genders", genders)
    put("genderCountMap", genderCountMap)
    forward()
  }
}
