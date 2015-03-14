package org.openurp.edu.eams.teach.grade.course.service.impl

import java.util.Collection
import java.util.Collections
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.Operation
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradePublishListener
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.CourseGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants

import scala.collection.JavaConversions._

class RecalcGpPublishListener extends CourseGradePublishListener {

  protected var calculator: CourseGradeCalculator = _

  def onPublish(grade: CourseGrade, gradeTypes: Array[GradeType]): List[Operation] = {
    for (gradeType <- gradeTypes if gradeType.getId == GradeTypeConstants.MAKEUP_ID || gradeType.getId == GradeTypeConstants.DELAY_ID) {
      calculator.calc(grade, null)
      return Operation.saveOrUpdate(grade).build()
    }
    Collections.emptyList()
  }

  def onPublish(grades: Collection[CourseGrade], gradeState: CourseGradeState, gradeTypes: Array[GradeType]): List[Operation] = {
    val operations = CollectUtils.newArrayList()
    var hasMakeupOrDelay = false
    for (gradeType <- gradeTypes if gradeType.getId == GradeTypeConstants.MAKEUP_ID || gradeType.getId == GradeTypeConstants.DELAY_ID) {
      hasMakeupOrDelay = true
      //break
    }
    if (!hasMakeupOrDelay) return operations
    for (grade <- grades) {
      calculator.calc(grade, gradeState)
      operations.addAll(Operation.saveOrUpdate(grade).build())
    }
    operations
  }

  def setCalculator(calculator: CourseGradeCalculator) {
    this.calculator = calculator
  }
}
