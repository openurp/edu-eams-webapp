package org.openurp.eams.grade

import org.openurp.teach.core.ProjectBasedEntity
import org.openurp.base.Semester
import org.openurp.teach.code.GradeType
import java.{ util => ju }

trait GradeInputSwitch extends ProjectBasedEntity[Integer] {

  /**
   * 教学日历
   */
  def semester: Semester

  def semester_=(semester: Semester)
  /**
   * 开始时间
   */
  def startAt: ju.Date

  /**
   * 关闭时间
   */
  def endAt: ju.Date

  /**
   * 允许录入成绩类型
   */
  def types: collection.mutable.HashSet[GradeType]

  def types_=(types: collection.mutable.HashSet[GradeType])
  /**
   * 成绩录入开关
   */
  def opened: Boolean

  /**
   * 备注
   */
  def remark: String
}