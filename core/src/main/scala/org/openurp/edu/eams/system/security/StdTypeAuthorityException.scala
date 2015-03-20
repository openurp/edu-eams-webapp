package org.openurp.edu.eams.system.security

import org.beangle.security.blueprint.User



@SerialVersionUID(7356207753232573651L)
class StdTypeAuthorityException(user: User, module: String) extends RuntimeException("StdTypeAuthorityException->[User:]" + user.name + 
  " [module:]" + 
  module + 
  "")
