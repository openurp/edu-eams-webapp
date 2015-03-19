package org.openurp.edu.eams.system.validate.model

import java.io.Serializable
import java.util.Date
import Challenge._




object Challenge {

  val SessionAttributeName = "ChallengeValidateToken"

  val ERROR = "validate"
}

@SerialVersionUID(-1761415667964522353L)
class Challenge( val challenge: String,  val effectiveAt: Date,  val invalidAt: Date)
    extends Serializable() {

  
  var valid: Boolean = _

  def isExpired(): Boolean = {
    System.currentTimeMillis() < effectiveAt.getTime || System.currentTimeMillis() >= invalidAt.getTime
  }
}
