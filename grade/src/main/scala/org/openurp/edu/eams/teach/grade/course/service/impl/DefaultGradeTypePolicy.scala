package org.openurp.edu.eams.teach.grade.course.service.impl

import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.service.GradeTypePolicy
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants



class DefaultGradeTypePolicy extends GradeTypePolicy {

  def isGradeFor(take: CourseTake, gradeType: GradeType, examtake: ExamTake): Boolean = {
    if (gradeType.id == GradeTypeConstants.DELAY_ID) return null != examtake && examtake.getExamType.id == ExamType.DELAY
    if (gradeType.id == GradeTypeConstants.MAKEUP_ID) return null != examtake && examtake.getExamType.id == ExamType.MAKEUP
    true
  }
}
