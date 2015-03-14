package org.openurp.edu.eams.teach.grade.service.impl

import java.util.List
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

trait GradeFilter {

  def filter(grades: List[CourseGrade]): List[CourseGrade]
}
