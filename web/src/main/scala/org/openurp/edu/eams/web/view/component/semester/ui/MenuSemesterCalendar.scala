package org.openurp.edu.eams.web.view.component.semester.ui




import org.openurp.base.Semester



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
          term.setId(semester.id)
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
