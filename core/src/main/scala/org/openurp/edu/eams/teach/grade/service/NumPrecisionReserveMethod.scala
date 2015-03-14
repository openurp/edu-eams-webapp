package org.openurp.edu.eams.teach.grade.service


import scala.collection.JavaConversions._

trait NumPrecisionReserveMethod {

  def reserve(num: Float, precision: Int): Float

  def reserve(num: Double, precision: Int): Double
}
