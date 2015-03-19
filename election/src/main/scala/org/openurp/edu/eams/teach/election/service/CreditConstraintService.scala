package org.openurp.edu.eams.teach.election.service

import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.election.model.constraint.AbstractCreditConstraint
import org.openurp.edu.eams.teach.election.model.constraint.StdCourseCountConstraint
import org.openurp.edu.eams.teach.election.model.constraint.StdTotalCreditConstraint



trait CreditConstraintService {

  def initStdTotalCreditConstraint(project: Project): String

  def getCreditConstraint(semester: Semester, std: Student): AbstractCreditConstraint

  def getTotalCreditConstraint(std: Student): StdTotalCreditConstraint

  def getCourseCountConstraint(semester: Semester, std: Student): StdCourseCountConstraint
}
