package org.openurp.edu.eams.teach.grade.service.stat

import java.util.Comparator


import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.service.impl.GradeFilter
import org.openurp.edu.teach.grade.CourseGrade




class StdTermGrade extends StdGrade() {

  
  var semester: Semester = _

  
  var awardedCredit: java.lang.Float = _

  def this(std: Student, 
      grades: List[CourseGrade], 
      cmp: Comparator[CourseGrade], 
      gradeFilters: List[GradeFilter]) {
    super(std, grades, cmp, gradeFilters)
  }

  def getElectedCredit(): java.lang.Float = {
    if (null == grades || grades.isEmpty) return new java.lang.Float(0)
    var credits = 0
    var iter = grades.iterator()
    while (iter.hasNext) {
      val courseGrade = iter.next()
      credits += courseGrade.getCourse.getCredits
    }
    new java.lang.Float(credits)
  }
}
