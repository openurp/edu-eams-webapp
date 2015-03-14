package org.openurp.edu.eams.teach.program.major.flexible.impl

import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.flexible.MajorProgramTextTitleProvider
//remove if not needed
import scala.collection.JavaConversions._

class DefaultMajorProgramTextTitleProvider extends MajorProgramTextTitleProvider {

  def provideTitle(program: Program): Array[String] = Array()
}
