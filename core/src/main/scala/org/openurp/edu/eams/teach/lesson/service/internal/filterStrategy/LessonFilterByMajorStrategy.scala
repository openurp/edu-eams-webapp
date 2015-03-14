package org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy

import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy.AbstractLessonFilterStrategy

import scala.collection.JavaConversions._

class LessonFilterByMajorStrategy extends AbstractLessonFilterStrategy("major") with LessonFilterStrategy {

  def getFilterString(): String = {
    " where lesson.teachClass.major.id = :id "
  }
}
