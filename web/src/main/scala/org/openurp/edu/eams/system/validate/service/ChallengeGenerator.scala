package org.openurp.edu.eams.system.validate.service

import org.openurp.edu.eams.system.validate.model.Challenge

import scala.collection.JavaConversions._

trait ChallengeGenerator {

  def gen(): Challenge

  def validate(challege: Challenge, response: String): Boolean

  def getTimeToLiveMinutes(): Int
}
