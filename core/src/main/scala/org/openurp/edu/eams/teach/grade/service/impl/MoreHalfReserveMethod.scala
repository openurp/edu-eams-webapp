package org.openurp.edu.eams.teach.grade.service.impl

import org.openurp.edu.eams.teach.grade.service.NumPrecisionReserveMethod

import scala.collection.JavaConversions._

class MoreHalfReserveMethod extends NumPrecisionReserveMethod {

  def reserve(num: Float, precision: Int): Float = {
    val mutilply = Math.pow(10, precision + 1).toInt
    num *= mutilply
    if (num % 10 >= 5) num += 10
    num -= num % 10
    num / mutilply
  }

  def reserve(num: Double, precision: Int): Double = {
    val mutilply = Math.pow(10, precision + 1).toInt
    num *= mutilply
    if (num % 10 >= 5) num += 10
    num -= num % 10
    num / mutilply
  }
}
