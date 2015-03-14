package org.openurp.edu.eams.web.view.component.semester.ui

import java.util.List
import java.util.Map
import org.openurp.edu.eams.base.Semester

import scala.collection.JavaConversions._

trait SemesterCalendarUI {

  def adapteItems(semesterTree: Map[String, List[Semester]]): AnyRef
}
