package org.openurp.edu.eams.teach.election.model.Enum


import scala.collection.JavaConversions._

object StdApplyType extends Enumeration {

  val APPLY_TYPE_ABSENCE = new StdApplyType()

  val APPLY_TYPE_GIVEUP = new StdApplyType()

  val APPLY_TYPE_OPEN_CLASS = new StdApplyType()

  val APPLY_TYPE_SUBSTITUTION = new StdApplyType()

  class StdApplyType extends Val {

    def getName(): String = this match {
      case APPLY_TYPE_ABSENCE => "申请免听"
      case APPLY_TYPE_GIVEUP => "本学期放弃"
      case APPLY_TYPE_OPEN_CLASS => "申请开班"
      case APPLY_TYPE_SUBSTITUTION => "申请替代"
      case _ => ""
    }
  }

  implicit def convertValue(v: Value): StdApplyType = v.asInstanceOf[StdApplyType]
}
