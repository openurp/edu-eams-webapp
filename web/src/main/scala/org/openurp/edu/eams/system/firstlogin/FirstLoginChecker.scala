package org.openurp.edu.eams.system.firstlogin

import org.beangle.security.blueprint.User

import scala.collection.JavaConversions._

trait FirstLoginChecker {

  def check(user: User): Boolean
}
