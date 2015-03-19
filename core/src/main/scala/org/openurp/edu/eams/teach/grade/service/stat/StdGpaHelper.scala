package org.openurp.edu.eams.teach.grade.service.stat

import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.teach.grade.service.GpaService



object StdGpaHelper {

  def statGpa(multiStdGrade: MultiStdGrade, gpaService: GpaService) {
    var stdGradeList = multiStdGrade.getStdGrades
    if (null == stdGradeList) {
      stdGradeList = Collections.emptyList()
    }
    for (stdGrade <- stdGradeList) {
      val stdGpa = new StdGpa(stdGrade.getStd)
      stdGpa.setGpa(gpaService.getGpa(stdGrade.getStd, stdGrade.grades))
      stdGrade.setStdGpa(stdGpa)
    }
  }
}
