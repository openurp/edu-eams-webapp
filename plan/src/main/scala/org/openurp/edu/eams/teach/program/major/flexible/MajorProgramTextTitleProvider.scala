package org.openurp.edu.eams.teach.program.major.flexible

import org.openurp.edu.eams.teach.program.Program
//remove if not needed


trait MajorProgramTextTitleProvider {

  def provideTitle(program: Program): Array[String]
}
