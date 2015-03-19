package org.openurp.edu.eams.teach.grade.course

import java.util.Date
import org.beangle.data.model.Entity
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType



trait CourseGradeLog extends Entity[Long] {

  def getStd(): Student

  def getCourse(): Course

  def getSemester(): Semester

  def getGradeType(): GradeType

  def getOldScore(): String

  def getNewScore(): String

  def getOldExamStatus(): ExamStatus

  def getNewExamStatus(): ExamStatus

  def getGradeId(): java.lang.Long

  def getUpdatedAt(): Date

  def getOperator(): String

  def getUpdatedFrom(): String

  def isRemove(): Boolean

  def getRemark(): String
}
