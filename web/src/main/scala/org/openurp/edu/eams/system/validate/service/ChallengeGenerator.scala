package org.openurp.edu.eams.system.validate.service

import org.openurp.edu.eams.system.validate.model.Challenge



trait ChallengeGenerator {

  def gen(): Challenge

  def validate(challege: Challenge, response: String): Boolean

  def getTimeToLiveMinutes(): Int
}
