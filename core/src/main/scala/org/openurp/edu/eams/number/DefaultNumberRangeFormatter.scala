package org.openurp.edu.eams.number

import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import DefaultNumberRangeFormatter._



object DefaultNumberRangeFormatter {

  private val me = new DefaultNumberRangeFormatter()

  def getInstance(): NumberRangeFormatter = me
}

class DefaultNumberRangeFormatter extends NumberRangeFormatter {

  def format(pattern: NumberRange, textResource: TextResource, hasNext: Boolean): String = {
    val sb = new StringBuilder()
    if (pattern.start == pattern.end) {
      sb.append(pattern.start)
    } else {
      sb.append('[').append(pattern.start).append('-')
        .append(pattern.end)
        .append(']')
    }
    if (Strings.isNotBlank(pattern.i18nKey)) {
      if (textResource != null) {
        sb.append(textResource(pattern.i18nKey))
      } else {
        sb.append(pattern.i18nKey)
      }
    }
    if (hasNext) {
      sb.append(',')
    }
    sb.toString
  }

  def format(pattern: NumberRange, textResource: TextResource): String = format(pattern, textResource, false)
}
