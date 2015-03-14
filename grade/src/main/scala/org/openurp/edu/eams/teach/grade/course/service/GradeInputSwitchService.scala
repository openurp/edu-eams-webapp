package org.openurp.edu.eams.teach.grade.course.service

import java.util.List
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch

import scala.collection.JavaConversions._

trait GradeInputSwitchService {

  def getSwitch(project: Project, semester: Semester): GradeInputSwitch

  def getOpenedSemesters(project: Project): List[Semester]
}
