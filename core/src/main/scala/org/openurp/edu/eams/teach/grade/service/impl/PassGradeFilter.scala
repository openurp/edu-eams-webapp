package org.openurp.edu.eams.teach.grade.service.impl

import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.functor.Predicate
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

class PassGradeFilter extends GradeFilter {

  def filter(grades: List[CourseGrade]): List[CourseGrade] = {
    val gradeList = CollectUtils.select(grades, new Predicate[CourseGrade]() {

      def apply(grade: CourseGrade): java.lang.Boolean = return grade.isPassed
    })
    gradeList
  }
}
