package org.openurp.edu.eams.teach.program.helper

import org.beangle.commons.lang.functor.Transformer
import org.openurp.edu.eams.teach.program.major.MajorPlan
import ProgramCollector._
//remove if not needed
import scala.collection.JavaConversions._

object ProgramCollector {

  val INSTANCE = new ProgramCollector()
}

class ProgramCollector private () extends Transformer {

  def apply(input: AnyRef): AnyRef = {
    input.asInstanceOf[MajorPlan].getProgram
  }


}
