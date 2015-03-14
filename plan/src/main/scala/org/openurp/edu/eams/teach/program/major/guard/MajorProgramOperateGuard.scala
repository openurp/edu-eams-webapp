package org.openurp.edu.eams.teach.program.major.guard

import java.util.List
import java.util.Map
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.exception.MajorProgramGuardException
//remove if not needed
import scala.collection.JavaConversions._

trait MajorProgramOperateGuard {

  def guard(operType: MajorProgramOperateType, program: Program, context: Map[String, Any]): Unit

  def guard(operType: MajorProgramOperateType, programs: List[Program], context: Map[String, Any]): Unit
}
