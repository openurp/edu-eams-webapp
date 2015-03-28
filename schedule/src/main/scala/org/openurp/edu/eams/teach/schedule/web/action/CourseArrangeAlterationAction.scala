package org.openurp.edu.eams.teach.schedule.web.action

import java.util.Date


import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.openurp.base.Semester
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.schedule.model.CourseArrangeAlteration
import org.openurp.edu.eams.teach.schedule.model.CourseMailSetting
import org.openurp.edu.eams.teach.schedule.service.CourseTableMailService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class CourseArrangeAlterationAction extends SemesterSupportAction {

  var courseTableMailService: CourseTableMailService = _

  def index(): String = {
    setSemesterDataRealm(hasStdType)
    forward()
  }

  def search(): String = {
    val semesterId = getInt("semester.id")
    val alterationAt = getDateTime("alterationAt")
    val endAt = getDateTime("endAt")
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.project.id=:projectid1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    if (semesterId != null) {
      builder.where("lesson.semester.id = :semesterId", semesterId)
      put("semester", entityDao.get(classOf[Semester], semesterId))
    }
    builder.where("lesson.schedule.status = 'ARRANGED'")
    populateConditions(builder)
    val teacherName = get("teacherName")
    if (Strings.isNotEmpty(teacherName)) {
      builder.join("lesson.teachers", "teacher")
      builder.where(Condition.like("teacher.name", teacherName))
    }
    var hqlString = builder.build().getStatement
    hqlString += " and alteration.lessonId = lesson.id"
    val query = OqlBuilder.from(classOf[CourseArrangeAlteration], "alteration")
    query.params(builder.getParams)
    query.where("exists (" + hqlString + ")")
    val alterationBefore = get("alteration.alterationBefore")
    val alterationAfter = get("alteration.alterationAfter")
    val alterFrom = get("alteration.alterFrom")
    val alterBy = get("alteration.alterBy.name")
    if (Strings.isNotEmpty(alterationBefore)) {
      query.where(Condition.like("alteration.alterationBefore", alterationBefore))
    }
    if (Strings.isNotEmpty(alterBy)) {
      query.where(Condition.like("alteration.alterBy.name", alterBy))
    }
    if (Strings.isNotEmpty(alterationAfter)) {
      query.where(Condition.like("alteration.alterationAfter", alterationAfter))
    }
    if (Strings.isNotEmpty(alterFrom)) {
      query.where(Condition.like("alteration.alterFrom", alterFrom))
    }
    if (semesterId != null) {
      query.where("alteration.semester.id = :alterSemesterId")
      query.param("alterSemesterId", semesterId)
    }
    if (null != alterationAt) {
      query.where("alteration.alterationAt >= :alterationAt", alterationAt)
      query.param("alterationAt", alterationAt)
    }
    if (null != endAt) {
      query.where("alteration.alterationAt <= :endAt", endAt)
      query.param("endAt", endAt)
    }
    query.limit(getPageLimit)
    val orderBy = get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      query.orderBy(Order.parse("alteration.alterationAt desc"))
    } else {
      query.orderBy(Order.parse(orderBy))
    }
    val alterations = entityDao.search(query)
    val alterationMap = Collections.newMap[Any]
    for (courseArrangeAlteration <- alterations) {
      alterationMap.put(courseArrangeAlteration, entityDao.get(classOf[Lesson], courseArrangeAlteration.getLessonId))
    }
    val mailBuilder = OqlBuilder.from(classOf[CourseMailSetting], "courseMailSetting")
    builder.where("courseMailSetting.creator.id = :userId", getUserId)
    builder.orderBy("courseMailSetting.updatedAt desc")
    val myCourseMailSettings = entityDao.search(mailBuilder)
    if (!myCourseMailSettings.isEmpty) {
      put("courseMailSettingId", myCourseMailSettings.get(0).id)
    }
    put("alterationMap", alterationMap)
    put("alterations", alterations)
    forward()
  }

  def info(): String = {
    val alterationId = getLongId("alteration")
    if (null == alterationId) {
      return forwardError("调课记录没有找到")
    }
    val alteration = entityDao.get(classOf[CourseArrangeAlteration], alterationId)
    val lesson = entityDao.get(classOf[Lesson], alteration.getLessonId)
    put("alteration", alteration)
    put("lesson", lesson)
    forward()
  }

  def sendEmail(): String = {
    val courseMailSettingId = getLong("courseMailSettingId")
    val alterationId = getLongId("alteration")
    if (null == alterationId) {
      return forwardError("调课记录没有找到")
    }
    var courseMailSetting: CourseMailSetting = null
    if (null == courseMailSettingId) {
      courseMailSetting = new CourseMailSetting()
      courseMailSetting.setTitle("调课通知")
      courseMailSetting.setName("默认模板")
      courseMailSetting.setModule("$(username),您好:\n\t我们将对$(lesson)进行调整,具体调整如下:$(content)本次调课生效时间为$(alterTime),特此通知.\n\t\t\t\t\t\t\t$(time)")
    } else {
      courseMailSetting = entityDao.get(classOf[CourseMailSetting], courseMailSettingId)
    }
    val courseArrangeAlteration = entityDao.get(classOf[CourseArrangeAlteration], alterationId)
    val catelog = getBoolean("catelog")
    var errorMsg = ""
    if (null != catelog) {
      errorMsg = if (true == catelog) courseTableMailService.sendCourseTableChangeMsgToTeacher(courseArrangeAlteration, 
        courseMailSetting) else courseTableMailService.sendCourseTableChangeMsgToStd(courseArrangeAlteration, 
        courseMailSetting)
    } else {
      val userIds = getLongIds("user")
      if (ArrayUtils.isEmpty(userIds)) {
        return forwardError("缺少参数")
      }
      errorMsg = courseTableMailService.sendCourseTableChangeMsg(courseArrangeAlteration, courseMailSetting, 
        userIds)
    }
    redirect("search", if (errorMsg == "") "邮件发送成功!" else errorMsg, get("params"))
  }

  def courseMailSetting(): String = {
    val courseMailSettings = entityDao.getAll(classOf[CourseMailSetting])
    val courseMailSetting = new CourseMailSetting()
    courseMailSetting.setTitle("调课通知")
    courseMailSetting.setName("默认模板")
    courseMailSetting.setModule("$(username),您好:\n\t我们将对$(lesson)进行调整,具体调整如下:$(content)本次调课生效时间为$(alterTime),特此通知.")
    put("defaultMailSetting", courseMailSetting)
    if (Collections.isNotEmpty(courseMailSettings)) {
      put("courseMailSettings", courseMailSettings)
      val builder = OqlBuilder.from(classOf[CourseMailSetting], "courseMailSetting")
      builder.where("courseMailSetting.creator.id = :userId", getUserId)
      builder.orderBy("courseMailSetting.updatedAt desc")
      val myCourseMailSettings = entityDao.search(builder)
      if (Collections.isNotEmpty(myCourseMailSettings)) {
        put("myCourseMailSetting", myCourseMailSettings.get(0))
      } else {
        put("myCourseMailSetting", courseMailSetting)
      }
    } else {
      put("courseMailSettings", CollectionUtils.EMPTY_COLLECTION)
      put("myCourseMailSetting", courseMailSetting)
    }
    put("closeBox", getBool("closeBox"))
    forward()
  }

  def courseMailSave(): String = {
    val courseMailSetting = populateEntity(classOf[CourseMailSetting], "courseMailSetting")
    val user = entityDao.get(classOf[User], getUserId)
    courseMailSetting.setCreator(user)
    val date = new Date()
    if (courseMailSetting.isTransient) {
      courseMailSetting.setCreatedAt(date)
    }
    courseMailSetting.setUpdatedAt(date)
    try {
      entityDao.save(courseMailSetting)
      redirect("courseMailSetting", "info.save.success", "closeBox=1")
    } catch {
      case e: Exception => redirect("courseMailSetting", "info.save.failure")
    }
  }

  def courseMailRemove(): String = {
    val courseMailSetting = entityDao.get(classOf[CourseMailSetting], getLongId("courseMailSetting"))
    try {
      entityDao.remove(courseMailSetting)
      redirect("courseMailSetting", "info.delete.success")
    } catch {
      case e: Exception => redirect("courseMailSetting", "info.delete.failure")
    }
  }

  def selectUsers(): String = {
    val nameOrFullname = get("term")
    val query = OqlBuilder.from(classOf[User], "user")
    populateConditions(query)
    if (Strings.isNotEmpty(nameOrFullname)) {
      query.where("(user.name like :name or user.fullname like :name)", '%' + nameOrFullname + '%')
    }
    query.where("user.effectiveAt <= :now and (user.invalidAt is null or user.invalidAt >= :now)", new Date())
    query.limit(getPageLimit)
    put("users", entityDao.search(query))
    forward("usersJSON")
  }
}
