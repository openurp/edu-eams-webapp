package org.openurp.edu.eams.teach.program.major.web.action

import org.beangle.commons.text.seq.HanZiSeqStyle
import HanZi2SeqStyle._
//remove if not needed
import scala.collection.JavaConversions._

object HanZi2SeqStyle {

  def main(args: Array[String]) {
    val s = new HanZi2SeqStyle()
    for (i <- 1 until 1101) {
      println(s.buildText(String.valueOf(i)))
    }
  }
}

class HanZi2SeqStyle extends HanZiSeqStyle {

  def buildText(str1: String): String = {
    val sb = new StringBuilder()
    var prev = 0
    for (i <- 0 until str1.length) {
      val numChar = str1.charAt(i)
      var temp = basicOf(numChar - '0')
      if (numChar - '0' > 0) {
        if (i - prev > 1) temp = CHINESE_NAMES(0) + temp
        prev = i
        temp = temp + priorityOf(str1.length - i)
        sb.append(temp)
      }
    }
    var result = sb.toString
    if (result.startsWith("一十")) result = result.substring(1)
    result
  }
}
