package org.openurp.edu.eams.system.firstlogin




trait PasswordValidator {

  def validate(password: String): String
}
