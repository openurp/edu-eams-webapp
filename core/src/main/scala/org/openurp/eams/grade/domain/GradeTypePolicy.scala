package org.openurp.eams.grade.domain

import org.openurp.teach.code.GradeType
import org.openurp.teach.exam.ExamTake
import org.openurp.teach.lesson.CourseTake

/**
 * 成绩给分策略<br>
 * 对于给定的学生选课记录是否在某种考试情况下，给予某一种成绩类型的成绩
 * 例如免修学生不给平时成绩等
 *
 * @author chaostone
 */
trait GradeTypePolicy {

  /**
   * 是否给予学生某种成绩
   */
  def shouldHaving(take: CourseTake, gradeType: GradeType, examtake: ExamTake): Boolean
}
