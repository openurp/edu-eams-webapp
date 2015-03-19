package org.openurp.edu.eams.number

import java.util.Arrays
import org.beangle.commons.text.i18n.TextResource
import org.openurp.edu.eams.weekstate.SemesterWeekTimeBuilder
import org.beangle.commons.lang.time.YearWeekTimeBuilder


object NumberRangeDigestor {

  def digest(numberSequence: Array[Int], textResource: TextResource): String = {
    digest(numberSequence, textResource, DefaultNumberRangeFormatter.getInstance)
  }

  def digest(numberSequence: Array[Int], textResource: TextResource, formatter: NumberRangeFormatter): String = {
    if (numberSequence == null || numberSequence.length == 0) {
      return ""
    }
    Arrays.sort(numberSequence)
    val patterns = new collection.mutable.ListBuffer[NumberRange]
    var lastPattern = NumberRange.newInstance(numberSequence(0))
    patterns.add(lastPattern)
    for (i <- 1 until numberSequence.length) {
      val number = numberSequence(i)
      if (!lastPattern.test(number)) {
        lastPattern = lastPattern.guessNextPattern(number)
        patterns.add(lastPattern)
      }
    }
    val sb = new StringBuilder()
    var iterator = patterns.iterator()
    while (iterator.hasNext) {
      val pattern = iterator.next()
      if (!pattern.isAbandon) {
        sb.append(formatter.format(pattern, textResource, iterator.hasNext))
      }
    }
    sb.toString
  }

  def digest(numberSequence: Array[Integer], textResource: TextResource): String = {
    if (numberSequence == null || numberSequence.length == 0) {
      return ""
    }
    val integers = Array.ofDim[Int](numberSequence.length)
    for (i <- 0 until numberSequence.length) {
      integers(i) = numberSequence(i)
    }
    digest(integers, textResource)
  }

  def digest(numberSequence: Array[Integer], textResource: TextResource, formatter: NumberRangeFormatter): String = {
    if (numberSequence == null || numberSequence.length == 0) {
      return null
    }
    val integers = Array.ofDim[Int](numberSequence.length)
    for (i <- 0 until numberSequence.length) {
      integers(i) = numberSequence(i)
    }
    digest(integers, textResource, formatter)
  }
}
