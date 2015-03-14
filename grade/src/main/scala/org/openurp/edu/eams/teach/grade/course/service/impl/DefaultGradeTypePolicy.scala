package org.openurp.edu.eams.teach.grade.course.service.impl

import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.course.service.GradeTypePolicy
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.ExamTake
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants

import scala.collection.JavaConversions._

class DefaultGradeTypePolicy extends GradeTypePolicy {

  def isGradeFor(take: CourseTake, gradeType: GradeType, examtake: ExamTake): Boolean = {
    if (gradeType.getId == GradeTypeConstants.DELAY_ID) return null != examtake && examtake.getExamType.getId == ExamType.DELAY
    if (gradeType.getId == GradeTypeConstants.MAKEUP_ID) return null != examtake && examtake.getExamType.getId == ExamType.MAKEUP
    true
  }
}
