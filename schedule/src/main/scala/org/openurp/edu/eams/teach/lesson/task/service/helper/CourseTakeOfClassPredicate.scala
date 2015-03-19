package org.openurp.edu.eams.teach.lesson.task.service.helper

import org.apache.commons.collections.Predicate
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.lesson.CourseTake



class CourseTakeOfClassPredicate(private var adminClass: Adminclass) extends Predicate {

  def evaluate(arg0: AnyRef): Boolean = {
    val courseTake = arg0.asInstanceOf[CourseTake]
    courseTake.getStd.getAdminclass.id == adminClass
  }
}
