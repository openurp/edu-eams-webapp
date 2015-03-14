package org.openurp.edu.eams.teach.lesson.task.service.helper

import org.apache.commons.collections.Predicate
import org.beangle.commons.lang.Numbers
import org.openurp.edu.teach.lesson.CourseTake

import scala.collection.JavaConversions._

class CourseTakeOfStdNoPredicate(var isOdd: Boolean) extends Predicate {

  def evaluate(`object`: AnyRef): Boolean = {
    val take = `object`.asInstanceOf[CourseTake]
    val num = Numbers.toInt(take.getStd.getCode.substring(take.getStd.getCode.length - 1))
    if (isOdd) {
      if (num % 2 == 0) false else true
    } else {
      if (num % 2 == 0) true else false
    }
  }
}
