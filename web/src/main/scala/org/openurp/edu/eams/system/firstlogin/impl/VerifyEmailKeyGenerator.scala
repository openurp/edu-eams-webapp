package org.openurp.edu.eams.system.firstlogin.impl

import org.beangle.security.codec.EncryptUtil



class VerifyEmailKeyGenerator {

  private var secretKey: String = "Eams verify key"

  def generate(email: String): String = EncryptUtil.encode(email + secretKey)

  def verify(email: String, digest: String): Boolean = generate(email) == digest
}
