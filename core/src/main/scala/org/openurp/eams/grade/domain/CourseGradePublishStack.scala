package org.openurp.eams.grade.domain

import org.beangle.data.model.dao.Operation
import org.openurp.eams.grade.CourseGradeState
import org.openurp.teach.code.GradeType
import org.openurp.teach.grade.CourseGrade

/**
 * 成绩发布监听器堆栈
 *
 * @author chaostone
 */
class CourseGradePublishStack(listeners: List[CourseGradePublishListener]) {

  def onPublish(grade: CourseGrade, gradeTypes: Array[GradeType]): Seq[Operation] = {
    val results = new collection.mutable.ListBuffer[Operation]
    for (listener <- listeners) {
      results ++= listener.onPublish(grade, gradeTypes)
    }
    results
  }

  def onPublish(grades: Iterable[CourseGrade], gradeState: CourseGradeState, gradeTypes: Array[GradeType]): Seq[Operation] = {
    val results = new collection.mutable.ListBuffer[Operation]
    for (listener <- listeners) {
      results ++= listener.onPublish(grades, gradeState, gradeTypes)
    }
    results
  }
}
