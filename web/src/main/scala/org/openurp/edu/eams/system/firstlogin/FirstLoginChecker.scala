package org.openurp.edu.eams.system.firstlogin

import org.beangle.security.blueprint.User



trait FirstLoginChecker {

  def check(user: User): Boolean
}
