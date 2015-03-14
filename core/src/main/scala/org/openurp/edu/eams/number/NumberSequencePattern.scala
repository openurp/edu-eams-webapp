package org.openurp.edu.eams.number


object NumberSequencePattern extends Enumeration {

  val CONTINUE = new NumberSequencePattern()

  val EVEN = new NumberSequencePattern()

  val ODD = new NumberSequencePattern()

  class NumberSequencePattern extends Val

  implicit def convertValue(v: Value): NumberSequencePattern = v.asInstanceOf[NumberSequencePattern]
}
