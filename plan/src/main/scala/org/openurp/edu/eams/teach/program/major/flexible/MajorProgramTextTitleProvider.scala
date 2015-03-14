package org.openurp.edu.eams.teach.program.major.flexible

import org.openurp.edu.eams.teach.program.Program
//remove if not needed
import scala.collection.JavaConversions._

trait MajorProgramTextTitleProvider {

  def provideTitle(program: Program): Array[String]
}
