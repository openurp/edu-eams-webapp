package org.openurp.edu.eams.system.security.web.action

import org.beangle.ems.security.helper.ProfileHelper
import org.beangle.security.blueprint.Role
import org.beangle.struts2.annotation.Action

import scala.collection.JavaConversions._

@Action("/security/role")
class RoleAction extends org.beangle.ems.security.web.action.RoleAction {

  override def editProfile(): String = {
    val role = entityDao.get(classOf[Role], getId("role", classOf[Integer]))
    val helper = new ProfileHelper(entityDao, securityHelper.getProfileService)
    helper.fillEditInfo(role, getUserId, isAdmin)
    put("role", role)
    forward()
  }
}
