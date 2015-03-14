package org.openurp.edu.eams.teach.program.major.web.action

import org.beangle.commons.text.seq.HanZiSeqStyle
import LuomaSeqStyle._
//remove if not needed
import scala.collection.JavaConversions._

object LuomaSeqStyle {

  val MAX = 99999

  val levels = Array(Array("I", "V", "X"), Array("X", "L", "C"), Array("C", "D", "M"))

  def main(args: Array[String]) {
    val luomaSeqStyle = new LuomaSeqStyle()
    for (i <- 0 until 1000) {
      println(luomaSeqStyle.buildText(String.valueOf(i + 1)))
    }
  }
}

class LuomaSeqStyle extends HanZiSeqStyle {

  def buildText(f: String): String = {
    var n = f
    if (!isNuneric(n)) {
      for (c <- 0 until n.length) {
        val chr = n.toLowerCase().charAt(c)
        if (chr != 'i' & chr != 'v' & chr != 'x' & chr != 'l' & chr != 'c' & 
          chr != 'd' & 
          chr != 'm') {
          return null
        }
      }
    }
    n = toRoman(f)
    n
  }

  def toRoman(n: String): String = {
    var r = ""
    for (c <- 0 until n.length) r += calcDigit(java.lang.Integer.parseInt(n.charAt(c) + ""), n.length - c - 1)
    r
  }

  def calcDigit(d: java.lang.Integer, l: Int): String = {
    if (l > 2) {
      var str = ""
      var m = 1
      while (m <= d * Math.pow(10, l - 3)) {str += "M"m += 1
      }
      str
    } else if (d == 1) levels(l)(0) else if (d == 2) levels(l)(0) + levels(l)(0) else if (d == 3) levels(l)(0) + levels(l)(0) + levels(l)(0) else if (d == 4) levels(l)(0) + levels(l)(1) else if (d == 5) levels(l)(1) else if (d == 6) levels(l)(1) + levels(l)(0) else if (d == 7) levels(l)(1) + levels(l)(0) + levels(l)(0) else if (d == 8) levels(l)(1) + levels(l)(0) + levels(l)(0) + levels(l)(0) else if (d == 9) levels(l)(0) + levels(l)(2) else ""
  }

  def isNuneric(str: String): Boolean = {
    for (c <- 0 until str.length) {
      val chr = str.charAt(c)
      if (chr != '0' & chr != '1' & chr != '2' & chr != '3' & chr != '4' & 
        chr != '5' & 
        chr != '6' & 
        chr != '7' & 
        chr != '8' & 
        chr != '9') return false
    }
    true
  }
}
