package org.openurp.edu.eams.teach.grade.service

import java.util.Collection
import java.util.List
import org.beangle.commons.dao.Operation
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState

import scala.collection.JavaConversions._

trait CourseGradePublishListener {

  def onPublish(grade: CourseGrade, gradeTypes: Array[GradeType]): List[Operation]

  def onPublish(grades: Collection[CourseGrade], gradeState: CourseGradeState, gradeTypes: Array[GradeType]): List[Operation]
}
