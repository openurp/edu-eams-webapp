package org.openurp.eams.grade

/**
 * 考试成绩成绩状态
 *
 * @author chaostone
 */
trait ExamGradeState extends GradeState {

  /**
   * 课程成绩状态
   */
  def gradeState: CourseGradeState

  /**
   * 百分比
   */
  def percent: java.lang.Float

  /**
   * 获取备注
   */
  def remark: String
}
