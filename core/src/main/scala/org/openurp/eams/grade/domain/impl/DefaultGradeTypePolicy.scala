package org.openurp.eams.grade.domain.impl

import org.openurp.eams.grade.domain.GradeTypePolicy
import org.openurp.teach.exam.ExamTake
import org.openurp.teach.code.GradeType
import org.openurp.teach.lesson.CourseTake
import org.openurp.teach.code.ExamType

class DefaultGradeTypePolicy extends GradeTypePolicy {

  /**
   * 补考和缓考要符合考试类型
   */
  def shouldHaving(take: CourseTake, gradeType: GradeType, examtake: ExamTake): Boolean = {
    if (gradeType.id == GradeType.Delay) return null != examtake && examtake.examType.id == ExamType.Delay
    if (gradeType.id == GradeType.Makeup) return null != examtake && examtake.examType.id == ExamType.Makeup
    true
  }
}
