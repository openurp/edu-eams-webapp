package org.openurp.edu.eams.system.validate.model

import java.io.Serializable
import java.util.Date
import Challenge._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object Challenge {

  val SessionAttributeName = "ChallengeValidateToken"

  val ERROR = "validate"
}

@SerialVersionUID(-1761415667964522353L)
class Challenge(@BeanProperty val challenge: String, @BeanProperty val effectiveAt: Date, @BeanProperty val invalidAt: Date)
    extends Serializable() {

  @BooleanBeanProperty
  var valid: Boolean = _

  def isExpired(): Boolean = {
    System.currentTimeMillis() < effectiveAt.getTime || System.currentTimeMillis() >= invalidAt.getTime
  }
}
