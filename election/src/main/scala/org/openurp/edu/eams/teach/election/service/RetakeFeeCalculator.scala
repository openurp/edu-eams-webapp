package org.openurp.edu.eams.teach.election.service

import org.beangle.commons.lang.Strings
import bsh.Interpreter
import org.openurp.edu.eams.teach.election.RetakeFeeConfig
import org.openurp.edu.teach.lesson.CourseTake



class RetakeFeeCalculator {

  protected var interpreter: Interpreter = new Interpreter()

  def calPreRetakeFee(config: RetakeFeeConfig, courseTake: CourseTake): java.lang.Integer = {
    try {
      interpreter.set("courseTake", courseTake)
      if (Strings.isNotBlank(config.getFeeRuleScript)) {
        val result = interpreter.eval(config.getFeeRuleScript).asInstanceOf[java.lang.Integer]
        if (null != result) {
          return result
        }
      }
      config.getPricePerCredit
    } catch {
      case e: Exception => {
        e.printStackTrace()
        throw new RuntimeException(e)
      }
    }
  }

  def calPrice(config: RetakeFeeConfig, courseTake: CourseTake): Float = {
    val pricePerCredit = calPreRetakeFee(config, courseTake)
    courseTake.getLesson.getCourse.getCredits * pricePerCredit
  }
}
