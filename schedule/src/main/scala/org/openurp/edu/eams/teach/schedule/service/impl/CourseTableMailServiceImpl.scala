package org.openurp.edu.eams.teach.schedule.service.impl

import java.text.SimpleDateFormat
import java.util.Date



import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.notification.mail.JavaMailSender
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.service.UserService
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.system.mail.service.MailService
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.schedule.model.CourseArrangeAlteration
import org.openurp.edu.eams.teach.schedule.model.CourseMailSetting
import org.openurp.edu.eams.teach.schedule.service.CourseTableMailService




class CourseTableMailServiceImpl extends BaseServiceImpl with CourseTableMailService {

  
  var mailService: MailService = _

  
  var userService: UserService = _

  def sendCourseTableChangeMsg(courseArrangeAlteration: CourseArrangeAlteration, courseMailSetting: CourseMailSetting, userIds: Array[Long]): String = {
    val lesson = entityDao.get(classOf[Lesson], courseArrangeAlteration.getLessonId)
    val title = courseMailSetting.getTitle
    var msg = courseMailSetting.getModule
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    msg = msg.replaceAll("\\n", "<br/>").replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
    msg = msg.replace("$(time)", sdf.format(new Date()))
    msg = msg.replace("$(lesson)", lesson.getNo + "(" + lesson.getCourse.getName + ")")
    msg = msg.replace("$(alterTime)", sdf.format(courseArrangeAlteration.getAlterationAt))
    msg = msg.replace("$(content)", "<table style='width:100%;border-collapse: collapse;border:solid;border-width:1px;border-color:#006CB2;vertical-align: middle;table-layout:fixed'><tr align='center'><td style='border-color:#006CB2;border-style:solid;border-width:0 1px 1px 0;overflow:hidden;word-wrap:break-word;'>" + 
      courseArrangeAlteration.getAlterationBefore.replaceAll(",", "<br/>") + 
      "</td><td style='border-color:#006CB2;border-style:solid;border-width:0 1px 1px 0;overflow:hidden;word-wrap:break-word;'>" + 
      courseArrangeAlteration.getAlterationAfter.replaceAll(",", "<br/>") + 
      "</td></tr></table>")
    val users = entityDao.get(classOf[User], userIds)
    var errorMsg = ""
    if (!users.isEmpty) {
      for (user <- users) {
        msg = msg.replace("$(username)", user.getFullname)
        try {
          mailService.sendMimeMail(title, msg, null, user.getMail)
        } catch {
          case e: Exception => {
            logger.info("info.sendMail.failure", e)
            errorMsg += user.getFullname + " 邮件发送失败"
          }
        }
      }
      errorMsg
    } else {
      "没有找到发送用户"
    }
  }

  def sendCourseTableChangeMsgToTeacher(courseArrangeAlteration: CourseArrangeAlteration, courseMailSetting: CourseMailSetting): String = {
    val lesson = entityDao.get(classOf[Lesson], courseArrangeAlteration.getLessonId)
    val title = courseMailSetting.getTitle
    var msgTemplate = courseMailSetting.getModule
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    msgTemplate = msgTemplate.replaceAll("\\n", "<br/>").replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
    msgTemplate = msgTemplate.replace("$(time)", sdf.format(new Date()))
    msgTemplate = msgTemplate.replace("$(lesson)", lesson.getNo + "(" + lesson.getCourse.getName + ")")
    msgTemplate = msgTemplate.replace("$(alterTime)", sdf.format(courseArrangeAlteration.getAlterationAt))
    msgTemplate = msgTemplate.replace("$(content)", "<table style='width:100%;border-collapse: collapse;border:solid;border-width:1px;border-color:#006CB2;vertical-align: middle;table-layout:fixed'><tr align='center'><td style='border-color:#006CB2;border-style:solid;border-width:0 1px 1px 0;overflow:hidden;word-wrap:break-word;'>" + 
      courseArrangeAlteration.getAlterationBefore.replaceAll(",", "<br/>") + 
      "</td><td style='border-color:#006CB2;border-style:solid;border-width:0 1px 1px 0;overflow:hidden;word-wrap:break-word;'>" + 
      courseArrangeAlteration.getAlterationAfter.replaceAll(",", "<br/>") + 
      "</td></tr></table>")
    val teachers = lesson.getTeachers
    var userNames = ""
    for (teacher <- teachers) {
      userNames += teacher.getCode + ","
    }
    var errorMsg = ""
    if (Strings.isNotEmpty(userNames)) {
      val mailAddresses = this.getUseMailByUserNames(userNames)
      for (username <- mailAddresses.keySet) {
        val msg = msgTemplate.replace("$(username)", username + "老师")
        try {
          mailService.sendMimeMail(title, msg, null, mailAddresses.get(username))
        } catch {
          case e: Exception => {
            logger.info("info.sendMail.failure", e)
            errorMsg += username + " 邮件发送失败"
          }
        }
      }
      errorMsg
    } else {
      "该课程没有指定教师,发送失败"
    }
  }

  def sendCourseTableChangeMsgToStd(courseArrangeAlteration: CourseArrangeAlteration, courseMailSetting: CourseMailSetting): String = {
    val lesson = entityDao.get(classOf[Lesson], courseArrangeAlteration.getLessonId)
    val title = courseMailSetting.getTitle
    var msg = courseMailSetting.getModule
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    msg = msg.replaceAll("\\n", "<br/>").replaceAll("\\t", "&nbsp;&nbsp;")
    msg = msg.replace("$(time)", sdf.format(new Date()))
    msg = msg.replace("$(lesson)", lesson.getNo + "(" + lesson.getCourse.getName + ")")
    msg = msg.replace("$(alterTime)", sdf.format(courseArrangeAlteration.getAlterationAt))
    msg = msg.replace("$(content)", "<table style='width:100%;border-collapse: collapse;border:solid;border-width:1px;border-color:#006CB2;vertical-align: middle;table-layout:fixed'><tr align='center'><td style='border-color:#006CB2;border-style:solid;border-width:0 1px 1px 0;overflow:hidden;word-wrap:break-word;'>" + 
      courseArrangeAlteration.getAlterationBefore.replaceAll(",", "<br/>") + 
      "</td><td style='border-color:#006CB2;border-style:solid;border-width:0 1px 1px 0;overflow:hidden;word-wrap:break-word;'>" + 
      courseArrangeAlteration.getAlterationAfter.replaceAll(",", "<br/>") + 
      "</td></tr></table>")
    val courseTakeSet = lesson.getTeachClass.getCourseTakes
    var userNames = ""
    for (take <- courseTakeSet) {
      userNames += take.getStd.getCode + ","
    }
    var errorMsg = ""
    if (Strings.isNotEmpty(userNames)) {
      val mailAddresses = this.getUseMailByUserNames(userNames)
      for (username <- mailAddresses.keySet) {
        msg = msg.replace("$(username)", username + "同学")
        try {
          mailService.sendMimeMail(title, msg, null, mailAddresses.get(username))
        } catch {
          case e: Exception => {
            logger.info("info.sendMail.failure", e)
            errorMsg += username + " 邮件发送失败"
          }
        }
      }
      errorMsg
    } else {
      "该课程没有学生选课,发送失败"
    }
  }

  def getUseMailByUserNames(userNames: String): Map[String, String] = {
    val userList = getUserList(userNames)
    val mailMap = Collections.newMap[Any]
    for (user <- userList if Strings.isNotEmpty(user.getMail)) {
      mailMap.put(user.getFullname, user.getMail)
    }
    mailMap
  }

  def getUserList(userNames: String): List[User] = {
    val query = OqlBuilder.from(classOf[User], "user")
    query.where("user.name in(:userNames)", userNames.split(","))
    entityDao.search(query)
  }
}
