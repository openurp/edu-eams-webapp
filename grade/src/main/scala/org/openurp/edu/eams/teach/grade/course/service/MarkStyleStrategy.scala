package org.openurp.edu.eams.teach.grade.course.service


import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.teach.grade.model.CourseGradeState



trait MarkStyleStrategy {

  def configMarkStyle(gradeState: CourseGradeState, gradeTypes: List[GradeType]): Unit
}
