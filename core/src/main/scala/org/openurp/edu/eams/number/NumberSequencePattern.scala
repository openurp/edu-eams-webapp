package org.openurp.edu.eams.number

object NumberSequencePattern extends Enumeration {

  val CONTINUE = new NumberSequencePattern()

  val EVEN = new NumberSequencePattern()

  val ODD = new NumberSequencePattern()

  class NumberSequencePattern extends Val

  import scala.language.implicitConversions

  implicit def convertValue(v: Value): NumberSequencePattern = v.asInstanceOf[NumberSequencePattern]
}
