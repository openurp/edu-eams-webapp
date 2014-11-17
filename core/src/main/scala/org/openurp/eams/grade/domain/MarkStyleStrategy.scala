package org.openurp.eams.grade.domain

import org.openurp.eams.grade.CourseGradeState
import org.openurp.teach.code.GradeType

/**
 * 课程成绩记录方式配置策略
 *
 * @author chaostone
 */
trait MarkStyleStrategy {

  /**
   * 针对空白的记录方式进行设置默认值
   */
  def configMarkStyle(gradeState: CourseGradeState, gradeTypes: Seq[GradeType]): Unit
}