package org.openurp.edu.eams.system.security

import org.beangle.security.blueprint.User

@SerialVersionUID(4154750932747027922L)
class DepartAuthorityException(user: User, module: String) extends RuntimeException("DepartAuthorityException->[User:]" + user.getName + " [module:]" + 
  module)
