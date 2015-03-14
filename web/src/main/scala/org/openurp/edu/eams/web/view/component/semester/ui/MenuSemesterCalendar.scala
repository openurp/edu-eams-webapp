package org.openurp.edu.eams.web.view.component.semester.ui

import java.util.ArrayList
import java.util.List
import java.util.Map
import org.openurp.edu.eams.base.Semester

import scala.collection.JavaConversions._

class MenuSemesterCalendar extends SemesterCalendarUI {

  def adapteItems(semesterTree: Map[String, List[Semester]]): AnyRef = {
    val results = new ArrayList[HierarchySemester]()
    for (schoolYear <- semesterTree.keySet) {
      val year = new HierarchySemester()
      year.setName(schoolYear)
      val semesters = semesterTree.get(schoolYear)
      if (null != semesters) {
        val hierarchySemesters = new ArrayList[HierarchySemester](semesters.size)
        for (semester <- semesters) {
          val term = new HierarchySemester()
          term.setId(semester.getId)
          term.setParent(year)
          term.setName(semester.getName)
          hierarchySemesters.add(term)
        }
        year.setChildren(hierarchySemesters)
      }
      results.add(year)
    }
    if (!results.isEmpty) {
      return results
    }
    null
  }
}
