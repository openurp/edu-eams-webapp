package org.openurp.edu.eams.system.security.web.action

import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.security.codec.EncryptUtil
import org.beangle.struts2.annotation.Action
import org.openurp.edu.eams.system.firstlogin.PasswordValidator
import org.openurp.edu.eams.system.firstlogin.web.action.AccountInitCheckAction
import org.openurp.edu.eams.system.security.model.EamsUserBean



@Action("/security/my")
class MyAction extends org.beangle.ems.security.web.action.MyAction {

  var passwordValidator: PasswordValidator = _

  def save(): String = {
    val userId = getUserId
    val user = entityDao.get(classOf[EamsUserBean], userId)
    val email = get("mail")
    val password = get("password")
    if (Strings.isNotEmpty(password)) {
      val msg = passwordValidator.validate(password)
      if (!Strings.isEmpty(msg)) return redirect("edit", msg)
      user.setPassword(EncryptUtil.encode(password))
      user.setDefaultPasswordUpdated(true)
    }
    if (Objects.!=(email, user.getMail)) {
      user.setMail(email)
      user.setMailVerified(false)
    }
    entityDao.saveOrUpdate(user)
    if (!user.isMailVerified) redirect(new org.beangle.struts2.convention.route.Action(classOf[AccountInitCheckAction], 
      "index"), "info.save.success") else redirect("infolet", "info.save.success")
  }
}
