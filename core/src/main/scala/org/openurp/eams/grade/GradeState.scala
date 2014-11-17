package org.openurp.eams.grade

import java.util.Date

import org.beangle.data.model.{ LongIdEntity, TemporalOn, Updated }
import org.openurp.teach.code.{ GradeType, ScoreMarkStyle }

/**
 * 成绩状态表<br>
 *
 * <pre>
 * 记录了对应教学任务成绩
 * 1)记录方式,
 * 2)各种成绩成分的百分比,
 * 3)各种成绩的确认状态,
 * 4)各种成绩的发布状态
 * </pre>
 *
 * @author 塞外狂人,chaostone
 */
trait GradeState extends LongIdEntity with TemporalOn with Updated {

  /**
   * 成绩类型
   */
  def gradeType: GradeType

  /**
   * 记录方式
   */
  def scoreMarkStyle: ScoreMarkStyle

  /**
   * 记录方式
   */
  def scoreMarkStyle_=(style: ScoreMarkStyle)

  /**
   * 是否提交确认
   */
  def confirmed: Boolean

  /**
   * 是否发布
   */
  def published: Boolean

  /**
   * 返回状态
   */
  def status: Int

  /**
   * 返回操作者
   */
  def operator: String
}
