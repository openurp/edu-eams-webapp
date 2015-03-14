package org.openurp.edu.eams.system.security

import org.beangle.security.blueprint.User

import scala.collection.JavaConversions._

@SerialVersionUID(7356207753232573651L)
class StdTypeAuthorityException(user: User, module: String) extends RuntimeException("StdTypeAuthorityException->[User:]" + user.getName + 
  " [module:]" + 
  module + 
  "")
