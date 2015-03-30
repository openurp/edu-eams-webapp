package org.openurp.edu.eams.weekstate

object BinaryConverter {

  def toString(number: java.lang.Long): String = java.lang.Long.toBinaryString(number)

  def toLong(binaryString: String): java.lang.Long = java.lang.Long.valueOf(binaryString, 2)
}
