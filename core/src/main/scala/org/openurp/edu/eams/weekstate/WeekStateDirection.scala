package org.openurp.edu.eams.weekstate


import scala.collection.JavaConversions._

object WeekStateDirection extends Enumeration {

  val LTR = new WeekStateDirection()

  val RTL = new WeekStateDirection()

  class WeekStateDirection extends Val

  implicit def convertValue(v: Value): WeekStateDirection = v.asInstanceOf[WeekStateDirection]
}
