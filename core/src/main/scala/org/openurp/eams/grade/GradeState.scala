package org.openurp.eams.grade

import org.beangle.data.model.LongIdEntity
import org.openurp.teach.code.GradeType
import org.openurp.teach.code.ScoreMarkStyle
import java.util.Date

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
trait GradeState extends LongIdEntity {

  /**
   * 成绩类型
   *
   * @return
   */
  def gradeType: GradeType

  /**
   * 记录方式
   *
   * @return
   */
  def scoreMarkStyle: ScoreMarkStyle

  /**
   * 返回保留小数位
   *
   * @return
   */
  def precision: Int

  /**
   * 设置小数位
   *
   * @param percision
   */
  def setPrecision(percision: Int): Unit

  /**
   * 录入时间
   *
   * @return
   */
  def inputedAt: Date

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
   *
   * @return
   */
  def operator: String
}
