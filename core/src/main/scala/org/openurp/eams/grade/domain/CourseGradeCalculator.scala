package org.openurp.eams.grade.domain

import org.openurp.eams.grade.CourseGradeState
import org.openurp.eams.grade.service.GradeRateService
import org.openurp.teach.grade.model.CourseGradeBean
import org.openurp.teach.grade.CourseGrade

/**
 * 成绩计算器
 *
 * @author chaostone
 */
trait CourseGradeCalculator {

  /**
   * 计算最终成绩
   * 一般是计算最终得分 MAX(GA,发布的补考成绩,缓考总评)+bonus
   *
   * @param grade
   * @return 计算结果,但不改动成绩
   */
  def calcScore(grade: CourseGrade): java.lang.Float

  /**
   * 计算总评成绩
   */
  def calcEndGa(grade: CourseGrade): java.lang.Float

  /**
   * 计算总评成绩
   */
  def calcDelayGa(grade: CourseGrade): java.lang.Float

  /**
   * 计算总评成绩
   */
  def calcMakeupGa(grade: CourseGrade): java.lang.Float
  /**
   * 计算总评成绩,最终成绩,是否通过和绩点
   *
   * @param grade
   */
  def calc(grade: CourseGrade): Unit

  /**
   * 更新最终
   */
  def updateScore(grade: CourseGradeBean, score: java.lang.Float): Unit
}
