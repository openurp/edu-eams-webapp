package org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy

import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy.AbstractLessonFilterStrategy

import scala.collection.JavaConversions._

class LessonFilterByDirectionStrategy extends AbstractLessonFilterStrategy("direction") with LessonFilterStrategy {

  def getFilterString(): String = {
    " where lesson.teachClass.direction.id= :id "
  }
}
