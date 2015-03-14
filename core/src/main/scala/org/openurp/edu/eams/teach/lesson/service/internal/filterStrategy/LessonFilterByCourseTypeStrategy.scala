package org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy

import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy.AbstractLessonFilterStrategy

import scala.collection.JavaConversions._

class LessonFilterByCourseTypeStrategy extends AbstractLessonFilterStrategy("courseType") with LessonFilterStrategy {

  def getFilterString(): String = " where lesson.courseType.id= :id "
}
