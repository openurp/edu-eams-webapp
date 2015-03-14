package org.openurp.edu.eams.system.firstlogin.web.action

import java.util.List
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.system.firstlogin.impl.VerifyEmailKeyGenerator
import org.openurp.edu.eams.system.security.model.EamsUserBean
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

class AccountMailVerifyAction extends BaseAction {

  def index(): String = {
    val mail = get("mail")
    val digest = get("key")
    var msg: String = null
    if (new VerifyEmailKeyGenerator().verify(mail, digest)) {
      val builder = OqlBuilder.from(classOf[EamsUserBean], "eu").where("eu.mail=:mail", mail)
      val users = entityDao.search(builder)
      var user: EamsUserBean = null
      if (users.size == 1) {
        user = users.get(0)
        if (!user.isMailVerified) {
          user.setMailVerified(true)
          entityDao.saveOrUpdate(user)
        }
      } else {
        msg = "没有找到符合该邮箱的唯一用户"
      }
    } else {
      msg = "错误的链接"
    }
    if (null != msg) put("result", msg)
    forward()
  }
}
