package org.openurp.edu.eams.system.message.web.action

import java.util.Date

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.system.msg.MessageContent
import org.openurp.edu.eams.system.msg.SystemMessage
import org.openurp.edu.eams.system.msg.SystemMessageType
import org.openurp.edu.eams.system.msg.model.SystemMessageBean
import org.openurp.edu.eams.system.msg.service.SystemMessageConfigService
import org.openurp.edu.eams.system.msg.service.SystemMessageService
import org.openurp.edu.eams.system.security.EamsUserCategory
import org.openurp.edu.eams.web.action.common.AbstractStudentProjectSupportAction



class SystemMessageForStdAction extends AbstractStudentProjectSupportAction {

  protected var systemMessageService: SystemMessageService = _

  protected var systemMessageConfigService: SystemMessageConfigService = _

  protected override def getEntityName(): String = classOf[SystemMessage].getName

  override def innerIndex(): String = {
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
    put("type", get("type"))
    put("isOpened", systemMessageConfigService.isOpened(getUserId, getProject.id))
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

  def saveDraft(): String = {
    if (!systemMessageConfigService.isOpened(getUserId, getProject.id)) {
      return forwardError("当前不开放")
    }
    val id = getLongId(getShortName)
    if (null == id) {
      return forwardError("没有要回复的消息")
    }
    val user = entityDao.get(classOf[User], getUserId)
    val message = systemMessageService.getReplyMessage(id, user.getName)
    if (null == message) {
      return forwardError("没有找到要回复的消息")
    }
    val content = populateEntity(classOf[MessageContent], "messageContent")
    if (content.isTransient) {
      content.getMessages.add(SystemMessageBean.getDefaultSystemMessage(content, message.getContent.getSender, 
        SystemMessageType.DRAFT))
      content.setCreatedAt(new Date())
    }
    content.setSender(user)
    put("type", get("type"))
    try {
      entityDao.saveOrUpdate(content)
      return redirect("index", "info.save.success", "type=" + get("type"))
    } catch {
      case e: Exception => logger.info("info.save.failure", e)
    }
    redirect("index", "info.save.failure", "type=" + get("type"))
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

  override def edit(): String = null

  override def save(): String = null

  override def importForm(): String = null

  override def importData(): String = null

  def setSystemMessageService(systemMessageService: SystemMessageService) {
    this.systemMessageService = systemMessageService
  }

  def setSystemMessageConfigService(systemMessageConfigService: SystemMessageConfigService) {
    this.systemMessageConfigService = systemMessageConfigService
  }
}
