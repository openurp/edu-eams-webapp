package org.openurp.eams.grade.service.stat

import org.openurp.teach.Project
import org.openurp.teach.code.GradeType
import java.util.Date
import org.beangle.commons.collection.Order

/**
 * 报表设置
 *
 * @author chaostone
 */
class GradeReportSetting {

  /**
   * 打印绩点
   */
  var printGpa: Boolean = true

  /**
   * 是否打印每学期绩点
   */
  var printTermGpa: Boolean = false

  /**
   * 打印成绩类型<br>
   */
  var gradeFilters: String = _

  /**
   * 每页打印的成绩数量
   */
  var pageSize = new java.lang.Integer(80)

  /**
   * 成绩中的字体大小
   */
  var fontSize = new java.lang.Integer(10)

  /**
   * 第一专业成绩
   */
  var project: Project = _

  /**
   * 打印奖励学分
   */
  var printAwardCredit: java.lang.Boolean = true

  /**
   * 是否打印校外考试成绩
   */
  var printOtherGrade: java.lang.Boolean = true

  /**
   * 成绩依照什么进行排序,具体含义要依照报表样式
   */
  var order: Order = _

  /**
   * 打印成绩的类型
   */
  var gradeType: GradeType = new GradeType(FINAL_ID)

  /**
   * 打印责任人
   */
  var printBy: String = _

  /**
   * 打印模板
   */
  var template: String = _

  /**
   * 打印时间
   */
  var printAt: Date = new Date()

}
