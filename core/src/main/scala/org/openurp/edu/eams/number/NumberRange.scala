package org.openurp.edu.eams.number

import NumberRange._

import scala.collection.JavaConversions._

object NumberRange {

  def newInstance(number: Int): NumberRange = new ContinueRange(number)
}

abstract class NumberRange protected (number: Int) {

  protected val start = number

  protected var end: java.lang.Integer = number

  protected var lastEnd: java.lang.Integer = _

  protected var abandon: Boolean = false

  protected var i18nKey: String = _

  def getStart(): java.lang.Integer = start

  def getEnd(): java.lang.Integer = end

  def isAbandon(): Boolean = abandon

  def getI18nKey(): String = i18nKey

  def test(number: Int): Boolean = {
    val result = internalTest(number)
    if (result && this.end != number) {
      this.lastEnd = this.end
      this.end = number
    }
    result
  }

  protected def internalTest(weekIndex: Int): Boolean

  def guessNextPattern(number: Int): NumberRange = {
    if (this.getClass == classOf[Skip1Range]) {
      return newInstance(number)
    }
    if (this.end != this.start) {
      return newInstance(number)
    }
    var mayBePattern: NumberRange = null
    mayBePattern = if (this.end % 2 == 0) new Skip1Range(this.end, "number.range.even") else new Skip1Range(this.end, 
      "number.range.odd")
    if (mayBePattern.test(number)) {
      if (this.lastEnd == null) {
        this.abandon = true
      }
      this.end = this.lastEnd
      return mayBePattern
    }
    newInstance(number)
  }
}
