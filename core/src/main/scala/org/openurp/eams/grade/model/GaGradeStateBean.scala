package org.openurp.eams.grade.model

import org.openurp.eams.grade.{CourseGradeState, GaGradeState}
import org.openurp.eams.grade.domain.AbstractGradeState
import org.openurp.teach.code.GradeType

/**
 * 考试成绩状态
 *
 * @author chaostone
 */
class GaGradeStateBean extends AbstractGradeState with GaGradeState {

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
}
