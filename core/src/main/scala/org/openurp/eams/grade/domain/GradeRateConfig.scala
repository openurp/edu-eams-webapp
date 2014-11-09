package org.openurp.eams.grade.domain

import java.text.NumberFormat

import org.beangle.data.model.bean.LongIdBean
import org.openurp.teach.Project
import org.openurp.teach.code.ScoreMarkStyle

import javax.persistence.Entity

/**
 * 成绩分级配置
 */
class GradeRateConfig extends LongIdBean {

  /**
   * 成绩记录方式
   */
  var scoreMarkStyle: ScoreMarkStyle = _

  /**
   * 对应培养类型(默认為空)
   */
  var project: Project = _

  /**
   * 成绩分级配置项
   */
  var items: List[GradeRateItem] = _

  /**
   * 及格线
   */
  var passScore: Float = _

  /**
   * 默认成绩
   */
  @transient
  private var defaultScoreMap = new collection.mutable.HashMap[String, Float]

  /**
   * 将字符串按照成绩记录方式转换成数字.<br>
   * 空成绩将转换成null
   *
   * @param score
   *            不能为空
   * @param markStyle
   * @return
   */
  def convert(grade: String): java.lang.Float = {
    defaultScoreMap.get(grade).asInstanceOf[java.lang.Float]
  }

  /**
   * 将字符串按照成绩记录方式转换成数字.<br>
   * 空成绩将转换成null
   *
   * @param score
   * @param markStyle
   * @return
   */
  def convert(score: java.lang.Float): String = {
    if (null == score) return ""
    if (scoreMarkStyle.numStyle) return NumberFormat.getInstance.format(score.floatValue())
    for (item <- items if item.contains(score)) {
      return item.grade
    }
    ""
  }

  /**
   * 1)将字符串的成绩等级转换成列表<br>
   * 2)将转换映射成map
   */
  def init() {
    var iterator = items.iterator
    while (iterator.hasNext) {
      val item = iterator.next()
      defaultScoreMap.put(item.grade, item.defaultScore)
    }
  }
}
