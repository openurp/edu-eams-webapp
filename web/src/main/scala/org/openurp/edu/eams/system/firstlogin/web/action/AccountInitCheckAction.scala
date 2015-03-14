package org.openurp.edu.eams.system.firstlogin.web.action

import javax.servlet.http.HttpServletRequest
import org.beangle.commons.config.Version
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.url.UrlBuilder
import org.beangle.security.blueprint.User
import org.beangle.security.core.AuthenticationException
import org.openurp.edu.eams.system.firstlogin.FirstLoginChecker
import org.openurp.edu.eams.system.firstlogin.PasswordValidator
import org.openurp.edu.eams.system.firstlogin.impl.VerifyEmailKeyGenerator
import org.openurp.edu.eams.system.mail.service.MailService
import org.openurp.edu.eams.system.security.model.EamsUserBean
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

class AccountInitCheckAction extends BaseAction with FirstLoginChecker {

  private var mailService: MailService = _

  private var systemVersion: Version = _

  private var passwordValidator: PasswordValidator = _

  def check(user: User): Boolean = {
    val euser = user.asInstanceOf[EamsUserBean]
    euser.isDefaultPasswordUpdated && euser.isMailVerified
  }

  def index(): String = {
    val userId = getUserId
    if (null == userId) throw new AuthenticationException("without login")
    val userb = entityDao.get(classOf[User], userId).asInstanceOf[EamsUserBean]
    put("user", userb)
    forward()
  }

  def sendVerifyEmail(): String = {
    val userId = getUserId
    if (null == userId) throw new AuthenticationException("without login")
    val userb = entityDao.get(classOf[User], userId).asInstanceOf[EamsUserBean]
    var success = false
    if (!userb.isMailVerified) {
      val email = get("user.mail").asInstanceOf[String]
      if (null != email) {
        userb.setMail(email)
        entityDao.saveOrUpdate(userb)
      }
      val request = getRequest
      val urlbuilder = new UrlBuilder(request.getContextPath)
      urlbuilder.serverName(request.getServerName).port(request.getServerPort)
      urlbuilder.servletPath("/accountMailVerify.action")
      urlbuilder.scheme(request.getScheme)
      val key = new VerifyEmailKeyGenerator().generate(userb.getMail)
      val builder = new StringBuilder(userb.getFullname + ",您好:<br>")
      builder.append("您已经输入<a href=\"mailto:" + userb.getMail + "\" target=\"_blank\">" + 
        userb.getMail + 
        "</a>作为联系您的电子邮件地址。" + 
        "我们只需验证该电子邮件地址是否属于您即可。您只需在登录" + 
        systemVersion.getName + 
        "后，点击下方链接即可。<br/><br/>" + 
        "<a target=\"_blank\" href=\"" + 
        urlbuilder.buildUrl() + 
        "?mail=" + 
        userb.getMail + 
        "&key=" + 
        key + 
        "\" style=\"color:#0088cc\">立即验证 &gt;</a>")
      try {
        mailService.sendMimeMail("请验证您" + systemVersion.getName + " 常用联系电子邮箱地址", builder.toString, null, 
          userb.getMail)
        success = true
      } catch {
        case e: Exception => {
          success = false
          logger.error("send  verify email[" + userb.getMail + "] error", e)
        }
      }
    }
    var msg = "发送成功"
    if (!success) msg = "发送失败"
    redirect("index", msg)
  }

  def update(): String = {
    val userId = getUserId
    if (null == userId) throw new AuthenticationException("without login")
    val userb = entityDao.get(classOf[User], userId).asInstanceOf[EamsUserBean]
    val password = get("user.password").asInstanceOf[String]
    if (null != password) {
      val msg = passwordValidator.validate(password)
      if (!Strings.isEmpty(msg)) {
        return redirect("index", msg)
      }
      userb.setPassword(password)
      userb.setDefaultPasswordUpdated(true)
    }
    val email = get("user.mail").asInstanceOf[String]
    if (null != email) {
      userb.setMail(email)
    }
    entityDao.saveOrUpdate(userb)
    redirect("index", "info.save.success")
  }

  def setMailService(mailService: MailService) {
    this.mailService = mailService
  }

  def setSystemVersion(systemVersion: Version) {
    this.systemVersion = systemVersion
  }

  def setPasswordValidator(passwordValidator: PasswordValidator) {
    this.passwordValidator = passwordValidator
  }
}
