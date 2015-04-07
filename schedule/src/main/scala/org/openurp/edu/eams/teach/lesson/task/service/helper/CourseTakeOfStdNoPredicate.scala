package org.openurp.edu.eams.teach.lesson.task.service.helper

import org.beangle.commons.lang.Numbers
import org.openurp.edu.teach.lesson.CourseTake



class CourseTakeOfStdNoPredicate(var isOdd: Boolean) extends Predicate {

  def evaluate(`object`: AnyRef): Boolean = {
    val take = `object`.asInstanceOf[CourseTake]
    val num = Numbers.toInt(take.std.code.substring(take.std.code.length - 1))
    if (isOdd) {
      if (num % 2 == 0) false else true
    } else {
      if (num % 2 == 0) true else false
    }
  }
}
