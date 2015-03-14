package org.openurp.edu.eams.system.validate.service.impl

import java.util.Calendar
import java.util.Date
import java.util.Random
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.system.validate.model.Challenge
import org.openurp.edu.eams.system.validate.service.ChallengeGenerator
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class NumberChallengeGenerator extends ChallengeGenerator {

  @BeanProperty
  var timeToLiveMinutes: Int = 15

  def gen(): Challenge = {
    val random = new Random()
    var validateCode = random.nextInt(999999) + ""
    if (validateCode.length < 6) {
      validateCode += Strings.repeat("0", 6 - validateCode.length)
    }
    val calendar = Calendar.getInstance
    calendar.add(Calendar.MINUTE, timeToLiveMinutes)
    new Challenge(validateCode, new Date(), calendar.getTime)
  }

  def validate(challenge: Challenge, response: String): Boolean = {
    !challenge.isExpired && challenge.getChallenge == response
  }
}
