package org.openurp.edu.eams.teach.program.log

import java.util.Map
//remove if not needed
import scala.collection.JavaConversions._

class InfoChain(someProperties: Map[String, String]) extends PhaseChain(someProperties) {

  def skipInfo(): EndChain = new EndChain(informations)

  def info(extraInfo: String): EndChain = {
    informations.put(MajorPlanLogHelper.INFO, extraInfo)
    new EndChain(informations)
  }
}
