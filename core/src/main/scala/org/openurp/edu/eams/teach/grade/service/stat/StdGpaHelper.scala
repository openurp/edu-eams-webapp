package org.openurp.edu.eams.teach.grade.service.stat

import org.openurp.edu.teach.grade.domain.StdGpa
import org.openurp.edu.teach.grade.service.GpaService
import scala.collection.mutable.ListBuffer

object StdGpaHelper {

  def statGpa(multiStdGrade: MultiStdGrade, gpaService: GpaService) {
    var stdGradeList = multiStdGrade.stdGrades
    if (null == stdGradeList) {
      stdGradeList = ListBuffer.empty
    }
    for (stdGrade <- stdGradeList) {
      val stdGpa = new StdGpa(stdGrade.std)
      stdGpa.gpa = gpaService.calcGpa(stdGrade.std, stdGrade.grades)
      stdGrade.stdGpa = stdGpa
    }
  }
}
