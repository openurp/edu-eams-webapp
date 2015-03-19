package org.openurp.edu.eams.teach.lesson.task.service.helper

import org.apache.commons.collections.Predicate
import org.openurp.edu.teach.lesson.CourseTake



class CourseTakeOfGenderPredicate(private var gender: String) extends Predicate {

  def evaluate(`object`: AnyRef): Boolean = {
    val take = `object`.asInstanceOf[CourseTake]
    var targetGender: String = null
    if (null != take.getStd.getGender) {
      targetGender = take.getStd.getGender.getName
    }
    gender == targetGender
  }
}
