package org.openurp.edu.eams.number

class Skip1Range(number: Int, i18nKey: String) extends NumberRange(number, i18nKey) {

  override def internalTest(number: Int): Boolean = {
    if (number == this.end) {
      return true
    }
    if (number - this.end == 2) {
      return true
    }
    false
  }
}
