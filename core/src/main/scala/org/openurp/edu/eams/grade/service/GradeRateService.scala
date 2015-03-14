package org.openurp.edu.eams.grade.service

import org.openurp.edu.eams.grade.model.GradeRateConfig
import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.teach.core.Project
import org.openurp.teach.grade.CourseGrade

/**
 * 绩点规则服务类
 * <p>
 * 提供以下服务
 * <li>转换成绩 convert</li>
 * <li>计算绩点 calGp</li>
 * <li>判断是否通过</li>
 * <li>查询支持的记录方式</li>
 */
trait GradeRateService {

  /**
   * 计算绩点
   */
  def calcGp(grade: CourseGrade): java.lang.Float

  /**
   * 将字符串按照成绩记录方式转换成数字.<br>
   * 空成绩将转换成null
   */
  def convert(score: String, scoreMarkStyle: ScoreMarkStyle, project: Project): java.lang.Float

  /**
   * 将字符串按照成绩记录方式转换成数字.<br>
   * 空成绩将转换成null
   */
  def convert(score: java.lang.Float, scoreMarkStyle: ScoreMarkStyle, project: Project): String

  /**
   */
  def isPassed(score: java.lang.Float, scoreMarkStyle: ScoreMarkStyle, project: Project): Boolean

  /**
   * 查询项目对应的记录方式的配置
   *
   */
  def getConfig(project: Project, scoreMarkStyle: ScoreMarkStyle): GradeRateConfig

  /**
   * 查询该项目对应的记录方式
   */
  def getMarkStyles(project: Project): List[ScoreMarkStyle]
}
