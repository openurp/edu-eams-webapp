package org.openurp.edu.eams.teach.grade.course.service

import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake



trait GradeTypePolicy {

  def isGradeFor(take: CourseTake, gradeType: GradeType, examtake: ExamTake): Boolean
}
