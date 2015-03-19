package org.openurp.edu.eams.teach.service



import org.openurp.base.CourseUnit



trait OccupyProcessor {

  def process(weekOccupy: Map[_,_], unit: CourseUnit, datas: List[_]): Unit
}
