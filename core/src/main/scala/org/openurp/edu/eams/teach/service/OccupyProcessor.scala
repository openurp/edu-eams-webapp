package org.openurp.edu.eams.teach.service



import org.openurp.base.CourseUnit



trait OccupyProcessor {

  def process(weekOccupy: collection.Map[_,_], unit: CourseUnit, datas: Iterable[Any]): Unit
}
