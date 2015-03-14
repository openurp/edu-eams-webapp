package org.openurp.edu.eams.teach.program.exporter

import java.text.NumberFormat
import FloatNumFormat._
//remove if not needed
import scala.collection.JavaConversions._

object FloatNumFormat {

  var instance: FloatNumFormat = new FloatNumFormat()

  def getInstance(): FloatNumFormat = instance
}

class FloatNumFormat {

  var numFormat: NumberFormat = NumberFormat.getInstance

  def format(num: Float): String = {
    if (num == 0) "" else numFormat.format(new java.lang.Double(num))
  }

  def getNumFormat(): NumberFormat = numFormat

  def setNumFormat(numFormat: NumberFormat) {
    this.numFormat = numFormat
  }
}
