package org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy

import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy.AbstractLessonFilterStrategy

import scala.collection.JavaConversions._

class LessonFilterByAdminclassStrategy extends AbstractLessonFilterStrategy("adminclass") with LessonFilterStrategy {

  def getFilterString(): String = {
    " inner join lesson.teachClass.courseTakes take where take.std.adminclass.id= :id "
  }
}
