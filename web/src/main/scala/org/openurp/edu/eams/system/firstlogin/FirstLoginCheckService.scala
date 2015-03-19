package org.openurp.edu.eams.system.firstlogin


import org.beangle.security.blueprint.User



trait FirstLoginCheckService {

  def check(user: User): Boolean

  def getCheckerNames(): Set[String]
}
