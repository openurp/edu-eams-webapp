package org.openurp.edu.eams.teach.grade.service.impl

import java.util.List

import scala.collection.JavaConversions._

trait GradeFilterRegistry {

  def getFilter(name: String): GradeFilter

  def getFilters(names: String): List[GradeFilter]
}
