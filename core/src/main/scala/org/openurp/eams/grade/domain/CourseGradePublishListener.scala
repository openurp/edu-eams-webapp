package org.openurp.eams.grade.domain

import org.beangle.data.model.dao.Operation
import org.openurp.eams.grade.CourseGradeState
import org.openurp.teach.CourseGrade
import org.openurp.teach.code.GradeType

/**
 * 成绩发布监听器
 * @author chaostone
 */
trait CourseGradePublishListener {

  /**
   * 发布单个成绩
   */
  def onPublish(grade: CourseGrade, gradeTypes: Array[GradeType]): Seq[Operation]

  /**
   * 发布一批成绩
   */
  def onPublish(grades: Iterable[CourseGrade], gradeState: CourseGradeState, gradeTypes: Array[GradeType]): Seq[Operation]
}
