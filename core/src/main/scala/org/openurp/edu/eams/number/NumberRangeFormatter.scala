package org.openurp.edu.eams.number

import org.beangle.commons.text.i18n.TextResource



trait NumberRangeFormatter {

  def format(pattern: NumberRange, textResource: TextResource, hasNext: Boolean): String

  def format(pattern: NumberRange, textResource: TextResource): String
}
