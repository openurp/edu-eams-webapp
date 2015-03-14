package org.openurp.edu.eams.teach.grade.course.service

import java.util.List
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.teach.grade.CourseGradeState

import scala.collection.JavaConversions._

trait MarkStyleStrategy {

  def configMarkStyle(gradeState: CourseGradeState, gradeTypes: List[GradeType]): Unit
}
