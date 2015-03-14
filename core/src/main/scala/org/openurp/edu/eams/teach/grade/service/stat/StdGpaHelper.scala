package org.openurp.edu.eams.teach.grade.service.stat

import java.util.Collections
import java.util.List
import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.teach.grade.service.GpaService

import scala.collection.JavaConversions._

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
