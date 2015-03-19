package org.openurp.edu.eams.web.view.component.semester.ui



import org.openurp.base.Semester



trait SemesterCalendarUI {

  def adapteItems(semesterTree: Map[String, List[Semester]]): AnyRef
}
