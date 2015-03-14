package org.openurp.edu.eams.teach.election

import org.beangle.commons.entity.Entity
import org.openurp.edu.eams.base.Semester
import org.openurp.code.edu.Education
import org.openurp.edu.teach.code.CourseType

import scala.collection.JavaConversions._

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
