package org.openurp.edu.eams.system.firstlogin.impl

import org.beangle.commons.lang.Chars
import org.openurp.edu.eams.system.firstlogin.PasswordValidator



class DefaultPasswordValidator extends PasswordValidator {

  def validate(password: String): String = {
    if (password.length < 6) return "密码的长度不应小于六位"
    var hasDigit = false
    var hasUpper = false
    var hasLower = false
    for (c <- password.toCharArray()) {
      if (java.lang.Character.isDigit(c)) {
        hasDigit = true
        //continue
      } else if (Chars.isAsciiAlpha(c)) {
        if (java.lang.Character.isUpperCase(c)) hasUpper = true else hasLower = true
      }
    }
    if (hasLower && hasUpper && hasDigit) "" else "新密码至少包含一位大写字母、一位小写字母和一位数字"
  }
}
