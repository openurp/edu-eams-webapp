package org.openurp.edu.eams.grade.domain

import org.beangle.data.model.dao.Operation
import org.openurp.edu.eams.grade.CourseGradeState
import org.openurp.teach.code.GradeType
import org.openurp.teach.grade.CourseGrade

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
