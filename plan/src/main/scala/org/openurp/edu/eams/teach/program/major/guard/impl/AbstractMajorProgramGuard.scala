package org.openurp.edu.eams.teach.program.major.guard.impl

import java.util.List
import java.util.Map
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.guard.MajorProgramOperateGuard
import org.openurp.edu.eams.teach.program.major.guard.MajorProgramOperateType
//remove if not needed
import scala.collection.JavaConversions._

abstract class AbstractMajorProgramGuard extends MajorProgramOperateGuard {

  def guard(operType: MajorProgramOperateType, program: Program, context: Map[String, Any]) {
    if (!iWantGuard(operType)) {
      return
    }
    doGuard(operType, program, context)
  }

  def guard(operType: MajorProgramOperateType, programs: List[Program], context: Map[String, Any]) {
    for (program <- programs) {
      guard(operType, program, context)
    }
  }

  protected def iWantGuard(operType: MajorProgramOperateType): Boolean

  protected def doGuard(operType: MajorProgramOperateType, program: Program, context: Map[String, Any]): Unit
}
