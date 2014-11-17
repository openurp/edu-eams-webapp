package org.openurp.eams.grade

/**
 * 考试成绩成绩状态
 *
 * @author chaostone
 */
trait GaGradeState extends GradeState {

  /**
   * 课程成绩状态
   */
  def gradeState: CourseGradeState

  /**
   * 获取备注
   */
  def remark: String
}
