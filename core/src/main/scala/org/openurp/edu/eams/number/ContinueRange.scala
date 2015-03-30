package org.openurp.edu.eams.number

class ContinueRange(start: Int, i18nKey: String) extends NumberRange(start, i18nKey) {

  override def internalTest(number: Int): Boolean = {
    if (number == this.end) {
      return true
    }
    if (number - this.end == 1) {
      return true
    }
    false
  }
}
