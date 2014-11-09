package org.openurp.eams.grade.domain

import org.beangle.data.model.bean.LongIdBean

/**
 * 成绩分级配置项
 */
class GradeRateItem extends LongIdBean {

  /**
   * 成绩配置
   */
  var config: GradeRateConfig = _

  /**
   * 显示名称
   */
  var grade: String = _

  /**
   * 最低分
   */
  var minScore: Float = _

  /**
   * 最高分
   */
  var maxScore: Float = _

  /**
   * 绩点表达式
   */
  var gpExp: String = _

  /**
   * 默认分数
   */
  var defaultScore: java.lang.Float = _

  def contains(f: java.lang.Float): Boolean = {
    minScore <= f.floatValue() && f.floatValue() <= maxScore
  }

  def inScope(score: java.lang.Float): Boolean = {
    maxScore.compareTo(score) > -1 && minScore.compareTo(score) < 1
  }
}
