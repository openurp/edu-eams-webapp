package org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy

import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy.AbstractLessonFilterStrategy



class LessonFilterByTeachDepartStrategy extends AbstractLessonFilterStrategy("teachDepart") with LessonFilterStrategy {

  def getFilterString(): String = " where lesson.teachDepart.id = :id "
}
