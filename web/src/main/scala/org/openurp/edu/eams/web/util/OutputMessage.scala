package org.openurp.edu.eams.web.util

import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource



class OutputMessage {

  protected var key: String = _

  protected var message: String = _

  def this(key: String, message: String) {
    this()
    this.key = key
    this.message = message
  }

  def this(key: String, message: String, engMessage: String) {
    this()
    this.key = key
    this.message = message
  }

  def getKey(): String = key

  def setKey(key: String) {
    this.key = key
  }

  def getMessage(): String = message

  def getMessage(textResource: TextResource): String = {
    if (Strings.isNotEmpty(key)) textResource.getText(key) else message
  }

  def setMessage(message: String) {
    this.message = message
  }
}
