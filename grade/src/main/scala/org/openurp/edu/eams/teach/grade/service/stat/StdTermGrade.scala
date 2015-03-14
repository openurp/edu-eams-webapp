package org.openurp.edu.eams.teach.grade.service.stat

import java.util.Comparator
import java.util.Iterator
import java.util.List
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.service.impl.GradeFilter
import org.openurp.edu.teach.grade.CourseGrade
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class StdTermGrade extends StdGrade() {

  @BeanProperty
  var semester: Semester = _

  @BeanProperty
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
