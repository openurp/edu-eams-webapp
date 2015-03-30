package org.openurp.edu.eams.teach.service

import org.openurp.base.CourseUnit
import org.openurp.edu.teach.lesson.CourseTake

trait OccupyProcessor {

  def process(weekOccupy: collection.mutable.Map[Any, Any], unit: CourseUnit, datas: Iterable[CourseTake]): Unit
}
