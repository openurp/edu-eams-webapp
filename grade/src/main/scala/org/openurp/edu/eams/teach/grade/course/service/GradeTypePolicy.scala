package org.openurp.edu.eams.teach.grade.course.service

import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.ExamTake

import scala.collection.JavaConversions._

trait GradeTypePolicy {

  def isGradeFor(take: CourseTake, gradeType: GradeType, examtake: ExamTake): Boolean
}
