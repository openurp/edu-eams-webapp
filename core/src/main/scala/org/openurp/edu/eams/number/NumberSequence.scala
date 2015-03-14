package org.openurp.edu.eams.number

import NumberSequencePattern._

object NumberSequence {

  def build(start: Int, end: Int, pattern: NumberSequencePattern): Array[Int] = {
    val integers = buildInteger(start, end, pattern)
    val ints = Array.ofDim[Int](integers.length)
    for (i <- 0 until integers.length) {
      ints(i) = integers(i)
    }
    ints
  }

  def buildInteger(start: Int, end: Int, pattern: NumberSequencePattern): Array[Integer] = {
    if (start > end) {
      return buildInteger(end, start, pattern)
    }
    val integers = new collection.mutable.ListBuffer[Integer]
    for (i <- start until end) {
      if (pattern == NumberSequencePattern.CONTINUE) {
        integers += i
      } else if (pattern == NumberSequencePattern.EVEN) {
        if (i % 2 == 0) {
          integers += i
        }
      } else {
        if (i % 2 != 0) {
          integers += i
        }
      }
    }
    integers.toArray(Array.ofDim[Integer](0))
  }
}
