package org.openurp.edu.eams.util

import org.beangle.commons.lang.Strings



object TimeUtils {

  def getTimeNumber(time: String): Int = getTimeNumber(time, ":")

  def getTimeNumber(time: String, delimter: String): Int = {
    val index = time.indexOf(delimter)
    java.lang.Integer.parseInt(time.substring(0, index) + time.substring(index + 1, index + 3))
  }

  def getTimeStr(time: Int): String = getTimeStr(time, ":")

  def getTimeStr(time: Int, delimter: String): String = {
    Strings.leftPad(String.valueOf(time / 100), 2, '0') + 
      delimter + 
      Strings.leftPad(String.valueOf(time % 100), 2, '0')
  }
}
