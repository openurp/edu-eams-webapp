package org.openurp.edu.eams.teach.program.log


//remove if not needed


class InfoChain(someProperties: Map[String, String]) extends PhaseChain(someProperties) {

  def skipInfo(): EndChain = new EndChain(informations)

  def info(extraInfo: String): EndChain = {
    informations.put(MajorPlanLogHelper.INFO, extraInfo)
    new EndChain(informations)
  }
}
