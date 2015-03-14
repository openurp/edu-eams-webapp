package org.openurp.edu.eams.teach.service

import java.util.List
import java.util.Map
import org.openurp.base.CourseUnit

import scala.collection.JavaConversions._

trait OccupyProcessor {

  def process(weekOccupy: Map[_,_], unit: CourseUnit, datas: List[_]): Unit
}
