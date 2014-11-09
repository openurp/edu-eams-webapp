package org.openurp.eams.grade.domain

import org.openurp.eams.grade.CourseGradeState
import org.openurp.teach.CourseGrade
import org.openurp.eams.grade.service.GradeRateService

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
  def calcScore(grade: CourseGrade, state: CourseGradeState): java.lang.Float

  /**
   * 计算总评成绩
   *
   * @param grade
   * @return 总评成绩,但不改动成绩
   */
  def calcGa(grade: CourseGrade, state: CourseGradeState): java.lang.Float

  /**
   * 计算总评成绩,最终成绩,是否通过和绩点
   *
   * @param grade
   */
  def calc(grade: CourseGrade, state: CourseGradeState): Unit

  /**
   * 更新最终
   */
  def updateScore(grade: CourseGrade, score: java.lang.Float): Unit

  /**
   * 得到用以转换成绩用的服务
   *
   * @return
   */
  def getGradeRateService(): GradeRateService
}
