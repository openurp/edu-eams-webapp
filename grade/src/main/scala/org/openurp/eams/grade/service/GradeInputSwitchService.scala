package org.openurp.eams.grade.service

import org.openurp.eams.grade.GradeInputSwitch
import org.openurp.teach.core.Project
import org.openurp.base.Semester

trait GradeInputSwitchService {

  /**
   */
  def getSwitch(project: Project, semester: Semester): GradeInputSwitch
}