package org.openurp.edu.eams.teach.grade.course.service


import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch



trait GradeInputSwitchService {

  def getSwitch(project: Project, semester: Semester): GradeInputSwitch

  def getOpenedSemesters(project: Project): List[Semester]
}
