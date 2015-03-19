package org.openurp.edu.eams.system.validate.web.action

import java.io.PrintWriter
import java.text.SimpleDateFormat
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.config.Version
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.url.UrlBuilder
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.function.FuncResource
import org.openurp.edu.eams.system.mail.service.MailService
import org.openurp.edu.eams.system.validate.model.Challenge
import org.openurp.edu.eams.system.validate.service.ChallengeGenerator
import org.openurp.edu.eams.web.action.BaseAction



class MailValidateAction extends BaseAction {

  private var mailService: MailService = _

  private var systemVersion: Version = _

  private var challengeGenerator: ChallengeGenerator = _

  def send(): String = {
    val user = entityDao.get(classOf[User], getUserId)
    val request = getRequest
    val response = getResponse
    response.setContentType("text/html")
    response.setCharacterEncoding("utf-8")
    val writer = response.getWriter
    val mail = user.getMail
    if (Strings.isEmpty(mail)) {
      writer.print("无法找到您的邮箱")
      return null
    }
    val resourceName = get("resource")
    var actionName = resourceName
    if (Strings.isEmpty(resourceName)) {
      writer.print("缺少resource资源参数")
      return null
    } else {
      if (actionName.contains(".action")) actionName = Strings.substringBefore(actionName, ".action")
      if (actionName.contains("!")) actionName = Strings.substringBefore(actionName, "!")
    }
    val resource = securityHelper.getFuncPermissionService.getResource(actionName)
    if (null == resource) {
      writer.print("查找不到给定的资源")
      return null
    }
    var params = get("params")
    val challenge = challengeGenerator.gen()
    if (Strings.isEmpty(params)) {
      params = "userresponse=" + challenge.getChallenge
    } else {
      params += "&userresponse=" + challenge.getChallenge
    }
    val urlbuilder = new UrlBuilder(request.getContextPath)
    urlbuilder.serverName(request.getServerName).port(request.getServerPort)
    urlbuilder.servletPath(resourceName + ".action")
    urlbuilder.queryString(params)
    urlbuilder.scheme(request.getScheme)
    val builder = new StringBuilder(user.getFullname + ",您好:<br>")
    builder.append("您本次在" + systemVersion.getName + "中," + resource.getTitle + 
      "的动态验证码为  " + 
      challenge.getChallenge + 
      "。有效时间:" + 
      challengeGenerator.getTimeToLiveMinutes + 
      "分钟，请在" + 
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(challenge.getInvalidAt) + 
      "之前使用。<br>")
    builder.append("也可以访问<a href=\"" + urlbuilder.buildUrl() + "\" target=\"_blank\">" + 
      resource.getTitle + 
      "</a>")
    getSession.put(Challenge.SessionAttributeName, challenge)
    try {
      mailService.sendMimeMail(resource.getTitle + "验证码 " + challenge.getChallenge, builder.toString, 
        null, mail)
      writer.print("已成功发送至邮箱:" + mail)
    } catch {
      case e: Exception => writer.print("邮件发送失败:" + e.getMessage)
    }
    null
  }

  def setMailService(mailService: MailService) {
    this.mailService = mailService
  }

  def setSystemVersion(systemVersion: Version) {
    this.systemVersion = systemVersion
  }

  def setChallengeGenerator(challengeGenerator: ChallengeGenerator) {
    this.challengeGenerator = challengeGenerator
  }
}
