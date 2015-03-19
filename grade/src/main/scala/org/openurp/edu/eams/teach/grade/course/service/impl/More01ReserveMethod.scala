package org.openurp.edu.eams.teach.grade.course.service.impl

import org.openurp.edu.eams.teach.grade.service.NumPrecisionReserveMethod



class More01ReserveMethod extends NumPrecisionReserveMethod {

  def reserve(num: Float, precision: Int): Float = {
    val mutilply = Math.pow(10, precision + 1).toInt
    var res = num * mutilply
    if (res % 10 >= 1) res += 10
    res -= res % 10
    res / mutilply
  }

  def reserve(num: Double, precision: Int): Double = {
    val mutilply = Math.pow(10, precision + 1).toInt
    var res = num * mutilply
    if (res % 10 >= 1) res += 10
    res -= res % 10
    res / mutilply
  }
}
