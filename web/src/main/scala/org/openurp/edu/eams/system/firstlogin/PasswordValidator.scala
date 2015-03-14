package org.openurp.edu.eams.system.firstlogin


import scala.collection.JavaConversions._

trait PasswordValidator {

  def validate(password: String): String
}
