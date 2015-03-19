package org.openurp.edu.eams.teach.election

import org.beangle.data.model.Entity
import org.openurp.base.Semester
import org.openurp.code.edu.Education
import org.openurp.edu.teach.code.CourseType



trait CourseTypeCreditConstraint extends Entity[Long] {

  def getGrades(): String

  def setGrades(grades: String): Unit

  def getSemester(): Semester

  def setSemester(semester: Semester): Unit

  def getEducation(): Education

  def setEducation(education: Education): Unit

  def getCourseType(): CourseType

  def setCourseType(courseType: CourseType): Unit

  def getLimitCredit(): Float

  def setLimitCredit(limitCredit: Float): Unit
}
