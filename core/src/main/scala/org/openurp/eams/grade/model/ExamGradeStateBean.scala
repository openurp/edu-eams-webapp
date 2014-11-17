package org.openurp.eams.grade.model

import org.openurp.eams.grade.{CourseGradeState, ExamGradeState}
import org.openurp.teach.code.GradeType
import org.openurp.eams.grade.domain.AbstractGradeState
import java.lang.{Short => JShort}

/**
 * 考试成绩状态
 *
 * @author chaostone
 */
class ExamGradeStateBean extends AbstractGradeState with ExamGradeState {

  /**
   * 成绩类型
   */
  var gradeType: GradeType = _

  /**
   * 总成绩状态
   */
  var gradeState: CourseGradeState = _

  /**
   * 备注
   */
  var remark: String = _

  /**
   * 百分比描述 <br>
   * 10% 就是 10， 20% 就是 20<br>
   */
  var percent: JShort = _
}