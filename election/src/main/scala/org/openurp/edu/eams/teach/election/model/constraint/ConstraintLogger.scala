package org.openurp.edu.eams.teach.election.model.constraint

import java.util.Date
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.election.CourseTypeCreditConstraint
import org.openurp.edu.eams.teach.election.model.Enum.ConstraintType
import ConstraintLogger._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object ConstraintLogger {

  def genLogger(constraint: StdCreditConstraint, `type`: String): ConstraintLogger = {
    val logger = new ConstraintLogger()
    logger.constraintType = ConstraintType.stdCreditConstraint
    val sb = new StringBuilder()
    sb.append(constraint.getSemester.getSchoolYear).append(constraint.getSemester.getName)
    sb.append(",").append(constraint.getStd.getCode)
    logger.semester = constraint.getSemester
    logger.key = sb.toString
    logger.value = if (null == constraint.getMaxCredit) null else constraint.getMaxCredit.toString
    logger.`type` = `type`
    logger
  }

  def genLogger(constraint: StdTotalCreditConstraint, `type`: String): ConstraintLogger = {
    val logger = new ConstraintLogger()
    logger.constraintType = ConstraintType.stdTotalCreditConstraint
    logger.key = constraint.getStd.getCode
    logger.value = if (null == constraint.getMaxCredit) null else constraint.getMaxCredit.toString
    logger.`type` = `type`
    logger
  }

  def genLogger(constraint: CourseTypeCreditConstraint, `type`: String): ConstraintLogger = {
    val logger = new ConstraintLogger()
    logger.constraintType = ConstraintType.courseTypeCreditConstraint
    val sb = new StringBuilder()
    sb.append(constraint.getSemester.getSchoolYear).append(constraint.getSemester.getName)
    sb.append(",[").append(constraint.grades).append("],")
    sb.append(",").append(constraint.education.getName)
    sb.append(",").append(constraint.getCourseType.getName)
      .append("(")
      .append(constraint.getCourseType.getCode)
      .append("),")
    logger.semester = constraint.getSemester
    logger.key = sb.toString
    logger.value = constraint.getLimitCredit + ""
    logger.`type` = `type`
    logger
  }

  def genLogger(constraint: StdCourseCountConstraint, courseType: CourseType, `type`: String): ConstraintLogger = {
    val logger = new ConstraintLogger()
    logger.constraintType = ConstraintType.stdCourseCountConstraint
    val sb = new StringBuilder()
    sb.append(constraint.getSemester.getSchoolYear).append(constraint.getSemester.getName)
    sb.append(",").append(constraint.getStd.getCode)
    if (null == courseType) {
      sb.append(",总门数上限")
      logger.value = if (null == constraint.getMaxCourseCount) null else constraint.getMaxCourseCount + ""
    } else {
      sb.append(",").append(courseType.getName).append("(")
        .append(courseType.getCode)
        .append(")")
      val maxCourseCount = constraint.getCourseTypeMaxCourseCount.get(courseType)
      logger.value = if (null == maxCourseCount) null else maxCourseCount + ""
    }
    logger.semester = constraint.getSemester
    logger.key = sb.toString
    logger.`type` = `type`
    logger
  }

  def genLogger(constraint: StdCourseCountConstraint, `type`: String): ConstraintLogger = genLogger(constraint, null, `type`)
}

@SerialVersionUID(-3518168114908289841L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.constraint.ConstraintLogger")
class ConstraintLogger extends LongIdObject() {

  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NotNull
  @Enumerated(value = EnumType.STRING)
  @BeanProperty
  var constraintType: ConstraintType = _

  @NotNull
  @Size(max = 30)
  @BeanProperty
  var `type`: String = _

  @NotNull
  @BeanProperty
  var key: String = _

  @BeanProperty
  var value: String = _

  @NotNull
  @BeanProperty
  var createdAt: Date = _

  @NotNull
  @BeanProperty
  var operator: String = _
}
