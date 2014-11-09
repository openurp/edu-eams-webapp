package org.openurp.eams.grade

import org.openurp.teach.code.GradeType
import org.openurp.teach.lesson.Lesson

/**
 * 课程成绩成绩状态
 *
 * @author chaostone
 */
trait CourseGradeState extends GradeState {

  /**
   * 教学任务
   *
   * @return
   */
  def lesson: Lesson

  /**
   * 是否为指定状态
   *
   * @param gradeType
   * @return
   */
  def isStatus(gradeType: GradeType, status: Int): Boolean

  /**
   * 更新状态
   */
  def updateStatus(gradeType: GradeType, status: Int): Unit

  /**
   * 返回指定成绩类型的成绩状态
   */
  def state(gradeType: GradeType): ExamGradeState

  /**
   * 所有成绩状态
   */
  def states: collection.Set[ExamGradeState]

  def percent(gradeType: GradeType): java.lang.Float

  /**
   * 获取其他录入人
   *
   * @return User
   */
  def extraInputer: String
}
