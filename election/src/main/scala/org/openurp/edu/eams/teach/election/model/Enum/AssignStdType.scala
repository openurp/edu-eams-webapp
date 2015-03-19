package org.openurp.edu.eams.teach.election.model.Enum




object AssignStdType extends Enumeration {

  val ALL = new AssignStdType()

  val ODD = new AssignStdType()

  val EVEN = new AssignStdType()

  class AssignStdType extends Val {

    def value(): Int = this match {
      case ODD => 1
      case EVEN => 1
      case _ => 0
    }
  }

  implicit def convertValue(v: Value): AssignStdType = v.asInstanceOf[AssignStdType]
}
