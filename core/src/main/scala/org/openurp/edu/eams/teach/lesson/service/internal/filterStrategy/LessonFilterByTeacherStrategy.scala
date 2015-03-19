package org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy

import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy.AbstractLessonFilterStrategy



class LessonFilterByTeacherStrategy extends AbstractLessonFilterStrategy("teacher") with LessonFilterStrategy {

  def getFilterString(): String = {
    " join lesson.teachers as teacher where teacher.id = :id "
  }
}
