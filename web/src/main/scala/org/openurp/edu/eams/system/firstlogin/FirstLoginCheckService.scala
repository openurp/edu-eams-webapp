package org.openurp.edu.eams.system.firstlogin

import java.util.Set
import org.beangle.security.blueprint.User

import scala.collection.JavaConversions._

trait FirstLoginCheckService {

  def check(user: User): Boolean

  def getCheckerNames(): Set[String]
}
