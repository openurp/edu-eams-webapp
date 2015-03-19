package org.openurp.edu.eams.system.message.web.action

import java.util.Date



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.service.UserService
import org.beangle.struts2.convention.route.Action
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.eams.system.msg.MessageContent
import org.openurp.edu.eams.system.msg.SystemMessage
import org.openurp.edu.eams.system.msg.SystemMessageType
import org.openurp.edu.eams.system.msg.model.SystemMessageBean
import org.openurp.edu.eams.system.msg.service.SystemMessageConfigService
import org.openurp.edu.eams.system.msg.service.SystemMessageService
import org.openurp.edu.eams.system.security.EamsUserCategory
import org.openurp.edu.eams.web.action.common.ProjectSupportAction



class SystemMessageAction extends ProjectSupportAction {

  private var userService: UserService = _

  override def edit(): String = {
    if (!systemMessageConfigService.isOpened(getUserId, getProject.id)) {
      return forwardError("当前不开放")
    }
    val entityId = getLongId("messageContent")
    var messageContent: MessageContent = null
    if (null == entityId) {
      messageContent = populateEntity(classOf[MessageContent], "messageContent")
      if (Strings.isNotBlank(get("recipientors"))) {
        val recipientors = Strings.split(get("recipientors"))
        for (recipientor <- recipientors) {
          val sm = new SystemMessageBean()
          sm.setRecipient(userService.get(recipientor))
          messageContent.getMessages.add(sm)
        }
      }
    } else {
      messageContent = entityDao.get(classOf[MessageContent], entityId)
    }
    if (null == messageContent) {
      return redirect("search", "操作的消息不存在")
    }
    put("type", get("type"))
    put("sender", entityDao.get(classOf[User], getUserId))
    put("messageContent", messageContent)
    editSetting(messageContent)
    forward()
  }

  def saveDraft(): String = {
    if (!systemMessageConfigService.isOpened(getUserId, getProject.id)) {
      return forwardError("当前不开放")
    }
    val usernames = getAll("user.name", classOf[String])
    if (Arrays.isEmpty(usernames)) {
      return forwardError("error.parameters.needed")
    }
    val recipientors = entityDao.get(classOf[User], "name", usernames)
    if (recipientors.isEmpty) {
      return forwardError("error.parameters.needed")
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
      var it = content.getMessages.iterator()
      while (it.hasNext) {
        val message = it.next()
        if (!recipientors.contains(message.getRecipient)) {
          it.remove()
        } else {
          message.setStatus(SystemMessageType.DRAFT)
          recipientors.remove(message.getRecipient)
        }
      }
      for (recipient <- recipientors) {
        content.getMessages.add(SystemMessageBean.getDefaultSystemMessage(content, recipient, SystemMessageType.DRAFT))
      }
      entityDao.saveOrUpdate(content)
      return redirect("index", "info.save.success", "type=" + get("type"))
    } catch {
      case e: Exception => logger.info("info.save.failure", e)
    }
    redirect("index", "info.save.success", "type=" + get("type"))
  }

  override def save(): String = {
    if (!systemMessageConfigService.isOpened(getUserId, getProject.id)) {
      return forwardError("当前不开放")
    }
    val usernames = getAll("user.name", classOf[String])
    if (Arrays.isEmpty(usernames)) {
      return forwardError("error.parameters.needed")
    }
    val recipientors = entityDao.get(classOf[User], "name", usernames)
    if (recipientors.isEmpty) {
      return forwardError("error.parameters.needed")
    }
    var success = false
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
      var it = content.getMessages.iterator()
      while (it.hasNext) {
        val message = it.next()
        if (!recipientors.contains(message.getRecipient)) {
          it.remove()
        } else {
          message.setStatus(SystemMessageType.NEWLY)
          recipientors.remove(message.getRecipient)
        }
      }
      for (recipient <- recipientors) {
        content.getMessages.add(SystemMessageBean.getDefaultSystemMessage(content, recipient, SystemMessageType.NEWLY))
      }
      content.setActiveOn(new Date())
      entityDao.saveOrUpdate(content)
      success = true
    } catch {
      case e: Exception => {
        success = false
        e.printStackTrace()
      }
    }
    val redirectAction = get("redirectAction")
    val redirectMethod = get("redirectMethod")
    doRedirect(success, redirectAction, redirectMethod)
  }

  private def doRedirect(success: Boolean, redirectAction: String, redirectMethod: String): String = {
    try {
      if (Strings.isNotBlank(redirectAction) && Strings.isNotBlank(redirectMethod)) {
        val actionClass = Class.forName(redirectAction)
        val action = new Action(actionClass, redirectMethod)
        if (success) {
          return redirect(action, "info.send.success")
        }
        return redirect(action, "info.send.failure")
      }
    } catch {
      case e: ClassNotFoundException => 
    }
    var `type` = get("type")
    `type` = if (Strings.isBlank(`type`)) "" else `type`
    if (success) {
      return redirect("index", "info.send.success", "type=" + `type`)
    }
    redirect("index", "info.send.failure", "type=" + `type`)
  }

  def searchUsers(): String = {
    if (!systemMessageConfigService.isOpened(getUserId, getProject.id)) {
      return forwardError("当前不开放")
    }
    val builder = OqlBuilder.from(classOf[User], "user")
    val usernames = getUsernames(get("usernames"))
    val notEmptyUsername = null != usernames && usernames.length > 0
    if (notEmptyUsername && usernames.length > 1) {
      populateConditions(builder, "user.name,user.id")
      builder.where("user.name in(:usernames)", usernames)
    } else {
      populateConditions(builder, "user.id")
      if (notEmptyUsername) {
        builder.where(Condition.like("user.name", usernames(0).toString))
      }
    }
    val pageLimit = getPageLimit
    val pageSize = get(QueryHelper.PAGESIZE)
    if (null == pageSize) {
      pageLimit.setPageSize(10)
    }
    builder.limit(pageLimit)
    var order = get(Order.ORDER_STR)
    if (Strings.isBlank(order)) {
      order = "user.name"
    }
    builder.orderBy(order)
    put("users", entityDao.search(builder))
    val sb = new StringBuilder()
    val recipientorUsernames = getUsernames(get("recipientors"))
    if (null != recipientorUsernames && recipientorUsernames.length > 0) {
      val recipientors = entityDao.get(classOf[User], "name", recipientorUsernames)
      val targetUsernames = new StringBuilder()
      for (recipientor <- recipientors) {
        sb.append(recipientor.getName).append("\n")
        targetUsernames.append(recipientor.getName).append(",")
      }
      put("targetUsernames", targetUsernames.toString)
    } else if (null != usernames) {
      for (username <- usernames) {
        sb.append(username).append("\n")
      }
    }
    put("usernames", sb.toString)
    "userList"
  }

  def addRecipientors(): String = {
    if (!systemMessageConfigService.isOpened(getUserId, getProject.id)) {
      return forwardError("当前不开放")
    }
    var recipientors = CollectUtils.newArrayList()
    val usernames = getUsernames(get("usernames"))
    if (null != usernames && usernames.length > 0) {
      recipientors = entityDao.get(classOf[User], "name", usernames)
    }
    put("recipientors", recipientors)
    forward()
  }

  private def getUsernames(usernameSeq: String): Array[Any] = {
    val usernames = Strings.split(usernameSeq, ",")
    val set = CollectUtils.newHashSet()
    if (null != usernames && usernames.length > 0) {
      for (username <- usernames if Strings.isNotBlank(username)) {
        set.add(username)
      }
    }
    set.toArray()
  }

  protected var systemMessageService: SystemMessageService = _

  protected var systemMessageConfigService: SystemMessageConfigService = _

  protected override def getEntityName(): String = classOf[SystemMessage].getName

  override def index(): String = {
    val menuProfileId = getUserCategoryId
    if (EamsUserCategory.MANAGER_USER == menuProfileId || EamsUserCategory.TEACHER_USER == menuProfileId) {
      put("type", "sended")
    } else {
      put("type", "inbox")
    }
    val username = getUsername
    put("newlyCount", systemMessageService.countNewly(username))
    put("draftCount", systemMessageService.countDraft(username))
    put("isOpened", systemMessageConfigService.isOpened(getUserId, getProject.id))
    indexSetting()
    forward()
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
    if (!systemMessageConfigService.isOpened(getUserId, getProject.id)) {
      return forwardError("当前不开放")
    }
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
    if (!systemMessageConfigService.isOpened(getUserId, getProject.id)) {
      return forwardError("当前不开放")
    }
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
    put("isOpened", systemMessageConfigService.isOpened(getUserId, getProject.id))
    putMessageType(getMessageType)
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
    putMessageType(getMessageType)
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

  override def importForm(): String = null

  override def importData(): String = null

  def setSystemMessageService(systemMessageService: SystemMessageService) {
    this.systemMessageService = systemMessageService
  }

  def setSystemMessageConfigService(systemMessageConfigService: SystemMessageConfigService) {
    this.systemMessageConfigService = systemMessageConfigService
  }

  def setUserService(userService: UserService) {
    this.userService = userService
  }
}
