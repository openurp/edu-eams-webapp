package org.openurp.edu.eams.teach.program.major.guard



import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.exception.MajorProgramGuardException
//remove if not needed


trait MajorProgramOperateGuard {

  def guard(operType: MajorProgramOperateType, program: Program, context: Map[String, Any]): Unit

  def guard(operType: MajorProgramOperateType, programs: List[Program], context: Map[String, Any]): Unit
}
