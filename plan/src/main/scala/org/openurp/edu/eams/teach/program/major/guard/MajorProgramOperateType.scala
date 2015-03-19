package org.openurp.edu.eams.teach.program.major.guard

//remove if not needed


object MajorProgramOperateType extends Enumeration {

  val CREATE = new MajorProgramOperateType()

  val UPDATE = new MajorProgramOperateType()

  val DELETE = new MajorProgramOperateType()

  val COPY = new MajorProgramOperateType()

  val AUDIT = new MajorProgramOperateType()

  class MajorProgramOperateType extends Val

  implicit def convertValue(v: Value): MajorProgramOperateType = v.asInstanceOf[MajorProgramOperateType]
}
