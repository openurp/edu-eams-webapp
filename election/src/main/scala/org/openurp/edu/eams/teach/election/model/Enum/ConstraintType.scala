package org.openurp.edu.eams.teach.election.model.Enum




object ConstraintType extends Enumeration {

  val stdCreditConstraint = new ConstraintType()

  val stdTotalCreditConstraint = new ConstraintType()

  val stdCourseCountConstraint = new ConstraintType()

  val courseTypeCreditConstraint = new ConstraintType()

  class ConstraintType extends Val {

    def getName(): String = this match {
      case stdCreditConstraint => "个人学分"
      case stdTotalCreditConstraint => "全程学分"
      case courseTypeCreditConstraint => "课程类别学分"
      case _ => "课程门数限制"
    }
  }

  implicit def convertValue(v: Value): ConstraintType = v.asInstanceOf[ConstraintType]
}
