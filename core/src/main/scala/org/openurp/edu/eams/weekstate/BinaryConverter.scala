package org.openurp.edu.eams.weekstate


import scala.collection.JavaConversions._

object BinaryConverter {

  def toString(number: java.lang.Long): String = java.lang.Long.toBinaryString(number)

  def toLong(binaryString: String): java.lang.Long = java.lang.Long.valueOf(binaryString, 2)
}
