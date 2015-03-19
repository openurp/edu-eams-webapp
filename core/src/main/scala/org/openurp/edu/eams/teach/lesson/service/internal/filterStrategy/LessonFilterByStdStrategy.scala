package org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy

import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy.AbstractLessonFilterStrategy



class LessonFilterByStdStrategy extends AbstractLessonFilterStrategy("std") with LessonFilterStrategy {

  def getFilterString(): String = {
    " join lesson.teachClass.courseTakes as takeInfo where (takeInfo.std.id= :id)"
  }
}
