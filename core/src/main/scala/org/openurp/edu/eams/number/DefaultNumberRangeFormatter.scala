package org.openurp.edu.eams.number

import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import DefaultNumberRangeFormatter._

import scala.collection.JavaConversions._

object DefaultNumberRangeFormatter {

  private val me = new DefaultNumberRangeFormatter()

  def getInstance(): NumberRangeFormatter = me
}

class DefaultNumberRangeFormatter extends NumberRangeFormatter {

  def format(pattern: NumberRange, textResource: TextResource, hasNext: Boolean): String = {
    val sb = new StringBuilder()
    if (pattern.getStart == pattern.getEnd) {
      sb.append(pattern.getStart)
    } else {
      sb.append('[').append(pattern.getStart).append('-')
        .append(pattern.getEnd)
        .append(']')
    }
    if (Strings.isNotBlank(pattern.getI18nKey)) {
      if (textResource != null) {
        sb.append(textResource(pattern.getI18nKey))
      } else {
        sb.append(pattern.getI18nKey)
      }
    }
    if (hasNext) {
      sb.append(',')
    }
    sb.toString
  }

  def format(pattern: NumberRange, textResource: TextResource): String = format(pattern, textResource, false)
}
