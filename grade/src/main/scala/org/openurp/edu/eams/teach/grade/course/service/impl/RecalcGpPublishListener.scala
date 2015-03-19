package org.openurp.edu.eams.teach.grade.course.service.impl


import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.Operation
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.service.CourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.CourseGradePublishListener
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants



class RecalcGpPublishListener extends CourseGradePublishListener {

  protected var calculator: CourseGradeCalculator = _

  def onPublish(grade: CourseGrade, gradeTypes: Array[GradeType]): List[Operation] = {
    for (gradeType <- gradeTypes if gradeType.id == GradeTypeConstants.MAKEUP_ID || gradeType.id == GradeTypeConstants.DELAY_ID) {
      calculator.calc(grade, null)
      return Operation.saveOrUpdate(grade).build()
    }
    Collections.emptyList()
  }

  def onPublish(grades: Iterable[CourseGrade], gradeState: CourseGradeState, gradeTypes: Array[GradeType]): List[Operation] = {
    val operations = CollectUtils.newArrayList()
    var hasMakeupOrDelay = false
    for (gradeType <- gradeTypes if gradeType.id == GradeTypeConstants.MAKEUP_ID || gradeType.id == GradeTypeConstants.DELAY_ID) {
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
