package org.openurp.eams.grade.domain

import java.util.Date
import org.openurp.eams.grade.GradeState
import org.beangle.data.model.bean.LongIdBean
import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.teach.grade.Grade

/**
 * 成绩状态抽象基类
 *
 * @author chaostone
 */
abstract class AbstractGradeState extends LongIdBean with GradeState {

  /**
   * 成绩记录方式
   */
  var scoreMarkStyle: ScoreMarkStyle = _

  /**
   * 成绩录入状态
   */
  var status: Int = Grade.Status.New

  /**
   * 小数点后保留几位
   */
  var precision: Int = 0

  /**
   * 上次成绩录入时间
   */
  var inputedAt: Date = _

  /**
   * 操作者
   */
  var operator: String = _

  /**
   * 确认的和发布的全部算作确认过的
   */
  def confirmed: Boolean = status >= Grade.Status.Confirmed

  def published: Boolean = status == Grade.Status.Published

}
