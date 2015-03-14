package org.openurp.edu.eams.teach.grade.service

import java.util.Collection
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.Operation
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState

import scala.collection.JavaConversions._

class CourseGradePublishStack {

  protected var listeners: List[CourseGradePublishListener] = CollectUtils.newArrayList()

  def onPublish(grade: CourseGrade, gradeTypes: Array[GradeType]): List[Operation] = {
    val results = CollectUtils.newArrayList()
    for (listener <- listeners) {
      results.addAll(listener.onPublish(grade, gradeTypes))
    }
    results
  }

  def onPublish(grades: Collection[CourseGrade], gradeState: CourseGradeState, gradeTypes: Array[GradeType]): List[Operation] = {
    val results = CollectUtils.newArrayList()
    for (listener <- listeners) {
      results.addAll(listener.onPublish(grades, gradeState, gradeTypes))
    }
    results
  }

  def setListeners(publishListeners: List[CourseGradePublishListener]) {
    this.listeners = publishListeners
  }
}
