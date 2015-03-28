package org.openurp.edu.eams.system.message.web.action
import java.util.Date



import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.system.msg.MessageContent
import org.openurp.edu.eams.system.msg.SystemMessage
import org.openurp.edu.eams.system.msg.SystemMessageType
import org.openurp.edu.eams.system.msg.model.SystemMessageBean
import org.openurp.edu.eams.system.msg.service.SystemMessageConfigService
import org.openurp.edu.eams.system.msg.service.SystemMessageService
import org.openurp.edu.eams.system.security.EamsUserCategory
import org.openurp.edu.eams.web.action.BaseAction



class SystemMessageForTeacherAction extends BaseAction {

  var systemMessageService: SystemMessageService = _

  var systemMessageConfigService: SystemMessageConfigService = _

  protected override def getEntityName(): String = classOf[SystemMessage].getName

  def index(): String = {
    val menuProfileId = getUserCategoryId
    if (EamsUserCategory.MANAGER_USER == menuProfileId || EamsUserCategory.TEACHER_USER == menuProfileId) {
      put("type", "sended")
    } else {
      put("type", "inbox")
    }
    val username = getUsername
    put("newlyCount", systemMessageService.countNewly(username))
    put("draftCount", systemMessageService.countDraft(username))
    put("isOpened", true)
    indexSetting()
    forward()
  }

  override def edit(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("权限不足")
    }
    val entityId = getLongId("messageContent")
    var entity: Entity[_] = null
    entity = if (null == entityId) populateEntity(classOf[MessageContent], "messageContent") else getModel(classOf[MessageContent].getName, 
      entityId)
    if (null == entity) {
      return redirect("search", "操作的消息不存在")
    }
    put("sender", entityDao.get(classOf[User], getUserId))
    put("messageContent", entity)
    editSetting(entity)
    forward()
  }

  def saveDraft(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("权限不足")
    }
    var stdCodes = getAll("student.code", classOf[String])
    val usernames = Collections.newBuffer[Any]
    if (stdCodes.length > 0) {
      val stdIdsSet = Collections.newHashSet(stdCodes)
      stdCodes = stdIdsSet.toArray(Array.ofDim[String](0))
      val stds = getStudents("code", false, stdCodes)
      if (stds.isEmpty) {
        return forwardError("权限不足")
      }
      for (student <- stds) {
        usernames.add(student.getCode)
      }
    }
    try {
      val content = populateEntity(classOf[MessageContent], "messageContent")
      val username = getUsername
      if (content.isPersisted && content.getSender.getName != username || 
        null != content.getActiveOn) {
        return forwardError("权限不足")
      } else {
        if (content.isTransient) {
          content.setSender(entityDao.get(classOf[User], getUserId))
          content.setCreatedAt(new Date())
        }
      }
      var users = Collections.emptySet()
      if (!usernames.isEmpty) {
        users = Collections.newHashSet(entityDao.get(classOf[User], "name", usernames))
      }
      var it = content.getMessages.iterator()
      while (it.hasNext) {
        val message = it.next()
        if (!users.contains(message.getRecipient)) {
          it.remove()
        } else {
          message.setStatus(SystemMessageType.DRAFT)
          users.remove(message.getRecipient)
        }
      }
      for (recipient <- users) {
        content.getMessages.add(SystemMessageBean.getDefaultSystemMessage(content, recipient, SystemMessageType.DRAFT))
      }
      entityDao.saveOrUpdate(content)
      return redirect("index", "info.save.success")
    } catch {
      case e: Exception => logger.info("info.save.failure", e)
    }
    redirect("index", "info.save.failure")
  }

  override def save(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("权限不足")
    }
    var stdCodes = getAll("student.code", classOf[String])
    if (Arrays.isEmpty(stdCodes)) {
      return forwardError("error.parameters.needed")
    }
    val stdIdsSet = Collections.newHashSet(stdCodes)
    stdCodes = stdIdsSet.toArray(Array.ofDim[String](0))
    val stds = getStudents("code", false, stdCodes)
    if (stds.isEmpty) {
      return forwardError("权限不足")
    }
    val usernames = Collections.newBuffer[Any]
    for (student <- stds) {
      usernames.add(student.getCode)
    }
    try {
      val content = populateEntity(classOf[MessageContent], "messageContent")
      val username = getUsername
      if (content.isPersisted && content.getSender.getName != username || 
        null != content.getActiveOn) {
        return forwardError("权限不足")
      } else {
        if (content.isTransient) {
          content.setSender(entityDao.get(classOf[User], getUserId))
          content.setCreatedAt(new Date())
        }
      }
      val users = Collections.newHashSet(entityDao.get(classOf[User], "name", usernames))
      var it = content.getMessages.iterator()
      while (it.hasNext) {
        val message = it.next()
        if (!users.contains(message.getRecipient)) {
          it.remove()
        } else {
          message.setStatus(SystemMessageType.NEWLY)
          users.remove(message.getRecipient)
        }
      }
      for (recipient <- users) {
        content.getMessages.add(SystemMessageBean.getDefaultSystemMessage(content, recipient, SystemMessageType.NEWLY))
      }
      content.setActiveOn(new Date())
      entityDao.saveOrUpdate(content)
      redirect("index", "info.send.success")
    } catch {
      case e: Exception => {
        e.printStackTrace()
        redirect("index", "info.send.failure")
      }
    }
  }

  def searchStds(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("权限不足")
    }
    val builder = OqlBuilder.from(classOf[Student], "student")
    val codes = getCodes(get("stdCodes"))
    if (codes.length > 1) {
      populateConditions(builder, "student.code,student.id")
      builder.where("student.code in(:codes)", codes)
    } else {
      populateConditions(builder, "student.id")
      if (codes.length > 0) {
        builder.where(Condition.like("student.code", codes(0).toString))
      }
    }
    builder.where("exists(" + 
      "from org.openurp.edu.teach.lesson.CourseTake courseTake " + 
      "join courseTake.lesson.teachers teacher " + 
      "where teacher=:teacher and courseTake.std=student and courseTake.lesson.semester=:semester)", 
      teacher, putSemester(null))
    val pageLimit = getPageLimit
    val pageSize = get(QueryHelper.PAGESIZE)
    if (null == pageSize) {
      pageLimit.setPageSize(10)
    }
    builder.limit(pageLimit)
    var order = get(Order.ORDER_STR)
    if (Strings.isBlank(order)) {
      order = "student.code"
    }
    builder.orderBy(order)
    put("students", entityDao.search(builder))
    val sb = new StringBuilder()
    val recipientorUsernames = getCodes(get("recipientors"))
    if (null != recipientorUsernames && recipientorUsernames.length > 0) {
      val stds = entityDao.get(classOf[Student], "code", recipientorUsernames)
      val targetStdCodes = new StringBuilder()
      for (student <- stds) {
        sb.append(student.getCode).append("\n")
        targetStdCodes.append(student.getCode).append(",")
      }
      put("targetStdCodes", targetStdCodes.toString)
    } else if (null != codes) {
      for (code <- codes) {
        sb.append(code).append("\n")
      }
    }
    put("codes", sb.toString)
    "stdList"
  }

  def addRecipientors(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("权限不足")
    }
    var students = Collections.newSet[Any]
    val addAll = getBool("addAll")
    val codes = getCodes(get("studentCodes"))
    students = getStudents("code", addAll, codes)
    put("students", students)
    forward()
  }

  private def getCodes(codeSeq: String): Array[Any] = {
    val codes = Strings.split(codeSeq, ",")
    val set = Collections.newSet[Any]
    if (null != codes && codes.length > 0) {
      for (code <- codes if Strings.isNotBlank(code)) {
        set.add(code)
      }
    }
    set.toArray()
  }

  private def getStudents(attrName: String, allStd: Boolean, values: AnyRef*): Set[Student] = {
    val stds = Collections.newSet[Any]
    val teacher = getLoginTeacher
    if (null != values && values.length > 0) {
      if (values.length < 500 || allStd) {
        val builder = OqlBuilder.from(classOf[Student], "std")
        builder.where("exists(" + 
          "from org.openurp.edu.teach.lesson.CourseTake courseTake " + 
          "join courseTake.lesson.teachers teacher " + 
          "where teacher=:teacher and courseTake.std=std and lesson.semester=:semester)", teacher, putSemester(null))
        if (!allStd) {
          builder.where("std." + attrName + " in(:values)", values)
        }
        stds.addAll(entityDao.search(builder))
      } else {
        var i = 0
        while (i < values.length) {
          var end = i + 500
          if (end > values.length) {
            end = values.length
          }
          val builder = OqlBuilder.from(classOf[Student], "std")
          builder.where("exists(" + 
            "from org.openurp.edu.teach.lesson.CourseTake courseTake " + 
            "join courseTake.lesson.teachers teacher " + 
            "where teacher=:teacher and courseTake.std=std and lesson.semester=:semester)", teacher, 
            putSemester(null))
          builder.where("std." + attrName + " in(:values)", Arrays.subarray(values, i, end))
          stds.addAll(entityDao.search(builder))
          i += 500
        }
      }
    }
    stds
  }

  override def search(): String = {
    val systemMessageType = getMessageType
    val `type` = get("type")
    if (`type` != null) {
      put("type", `type`.toUpperCase())
    } else {
      put("type", "inbox")
    }
    if (null == systemMessageType) {
      return forwardError("参数错误")
    }
    super.search()
  }

  protected override def getQueryBuilder[T <: Entity[_]](): OqlBuilder[T] = {
    var builder = super.getQueryBuilder
    val systemMessageType = getMessageType
    val username = getUsername
    var orderStr = get(Order.ORDER_STR)
    if (null != systemMessageType) {
      if (Strings.isBlank(orderStr)) {
        orderStr = "content.createdAt desc"
      }
      systemMessageType match {
        case INBOX => 
          builder.where("systemMessage.status in(:statuses)", Array(SystemMessageType.NEWLY, SystemMessageType.READED))
          builder.where("systemMessage.recipient.name=:username", username)

        case TRASH => 
          builder.where("systemMessage.status=:trash", SystemMessageType.TRASH)
          builder.where("systemMessage.recipient.name=:username", username)

        case DRAFT => 
          builder = OqlBuilder.from(classOf[MessageContent].getName + " messageContent")
          builder.where("messageContent.activeOn is null and messageContent.sender.name=:username", username)
          if (Strings.isBlank(get(Order.ORDER_STR))) {
            orderStr = "messageContent.createdAt desc"
          }

        case SENDED => 
          builder = OqlBuilder.from(classOf[MessageContent].getName + " messageContent")
          builder.where("messageContent.activeOn<=:now and (messageContent.invalidateOn is null or messageContent.invalidateOn>=:now) and messageContent.sender.name=:username", 
            new Date(), username)
          if (Strings.isBlank(get(Order.ORDER_STR))) {
            orderStr = "messageContent.createdAt desc"
          }

        case _}
    }
    builder.orderBy(Order.parse(orderStr))
    put("username", username)
    put("user", getUser)
    putMessageType(systemMessageType)
    builder.limit(getPageLimit)
  }

  def reply(): String = {
    val user = entityDao.get(classOf[User], getUserId)
    val contentId = getLong("messageContent.id")
    var content: MessageContent = null
    if (null != contentId) {
      val contents = systemMessageService.getMessageContent(user.getName, true, contentId)
      if (!contents.isEmpty) {
        content = contents.get(0)
        put("messageContent", content)
      }
    }
    if (null == content) {
      val id = getLongId(getShortName)
      if (null == id) {
        return forwardError("没有要回复的消息")
      }
      val message = systemMessageService.getReplyMessage(id, user.getName)
      if (null == message) {
        return forwardError("没有找到要回复的消息")
      }
      put("message", message)
    }
    put("sender", user)
    forward()
  }

  def sendReply(): String = {
    val user = entityDao.get(classOf[User], getUserId)
    val contentId = getLong("messageContent.id")
    var content: MessageContent = null
    if (null != contentId) {
      val contents = systemMessageService.getMessageContent(user.getName, true, contentId)
      if (!contents.isEmpty) {
        content = contents.get(0)
        for (message <- content.getMessages) {
          message.setStatus(SystemMessageType.NEWLY)
        }
        content.setActiveOn(new Date())
        try {
          entityDao.saveOrUpdate(content)
          return redirect("index", "info.send.success")
        } catch {
          case e: Exception => return redirect("index", "info.send.failure")
        }
      }
    }
    val id = getLongId(getShortName)
    if (null == id) {
      return forwardError("没有要回复的消息")
    }
    val message = systemMessageService.getReplyMessage(id, user.getName)
    if (null == message) {
      return forwardError("没有找到要回复的消息")
    }
    if (systemMessageService.reply(get("messageContent.subject"), get("messageContent.text"), id, getUsername)) {
      return redirect("index", "info.send.success")
    }
    redirect("index", "info.send.failure")
  }

  def info(): String = {
    val id = getLongId("messageContent")
    if (null == id) {
      return forwardError("没有找到消息")
    }
    val contents = systemMessageService.getMessageContent(getUsername, false, id)
    if (contents.isEmpty) {
      return forwardError("没有找到消息或者没有权限")
    }
    put("messageContent", contents.get(0))
    forward()
  }

  def readMessage(): String = {
    val visitIp = getRequest.getRemoteAddr
    val message = systemMessageService.readMessage(getLongId(getShortName), getUsername, visitIp, new Date())
    if (null == message) {
      return forwardError("没有找到消息或者没有权限")
    }
    put("systemMessage", message)
    put("type", get("type"))
    put("isOpened", true)
    putMessageType(SystemMessageType.INBOX)
    forward()
  }

  def infoMessage(): String = {
    val id = getLongId(getShortName)
    if (null == id) {
      return forwardError("没有找到消息")
    }
    val messages = systemMessageService.getMessages(getUsername, id)
    if (messages.isEmpty) {
      return forwardError("没有找到消息或者没有权限")
    }
    put("systemMessage", messages.get(0))
    putMessageType(SystemMessageType.TRASH)
    "readMessage"
  }

  def remove(): String = {
    redirect("index", systemMessageService.remove(getUsername, getLongIds(getShortName)), "type=" + get("type"))
  }

  def removeSended(): String = {
    redirect("index", systemMessageService.removeContent(getUsername, false, getLongIds("messageContent")), 
      "type=" + get("type"))
  }

  def removeDraft(): String = {
    redirect("index", systemMessageService.removeContent(getUsername, true, getLongIds("messageContent")), 
      "type=" + get("type"))
  }

  def toTrash(): String = {
    val visitIp = getRequest.getRemoteAddr
    redirect("index", systemMessageService.setMessageStatus(SystemMessageType.TRASH, getUsername, visitIp, 
      new Date(), false, getLongIds(getShortName)), "type=" + get("type"))
  }

  def restore(): String = {
    put("type", get("type"))
    redirect("index", systemMessageService.setMessageStatus(SystemMessageType.NEWLY, getUsername, true, 
      getLongIds(getShortName)), "type=" + get("type"))
  }

  def setReaded(): String = {
    put("type", get("type"))
    val visitIp = getRequest.getRemoteAddr
    redirect("index", systemMessageService.setMessageStatus(SystemMessageType.READED, getUsername, visitIp, 
      new Date(), true, getLongIds(getShortName)), "type=" + get("type"))
  }

  def setNewly(): String = {
    put("type", get("type"))
    redirect("index", systemMessageService.setMessageStatus(SystemMessageType.NEWLY, getUsername, true, 
      getLongIds(getShortName)), "type=" + get("type"))
  }

  protected def getMessageType(): SystemMessageType = {
    val `type` = get("type")
    if (Strings.isBlank(`type`)) SystemMessageType.INBOX else SystemMessageType.valueOf(`type`.toUpperCase())
  }

  protected def putMessageType(systemMessageType: SystemMessageType) {
    put("type", systemMessageType)
    put("INBOX", SystemMessageType.INBOX)
    put("DRAFT", SystemMessageType.DRAFT)
    put("SENDED", SystemMessageType.SENDED)
    put("INTRASH", SystemMessageType.TRASH)
    put("NEWLY", SystemMessageType.NEWLY)
    put("READED", SystemMessageType.READED)
  }
}
