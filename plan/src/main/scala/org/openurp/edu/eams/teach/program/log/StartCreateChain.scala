package org.openurp.edu.eams.teach.program.log

import java.util.Map
import org.openurp.edu.eams.teach.program.major.MajorPlan
//remove if not needed
import scala.collection.JavaConversions._

class StartCreateChain(informations: Map[String, String]) extends PhaseChain(informations) {

  informations.put(MajorPlanLogHelper.TYPE, "CREATE")

  def start(): BeforeChain = new BeforeChain(informations)

  def startWithPlan(plan: MajorPlan): BeforeChain = {
    initialLogInfo(plan)
    new BeforeChain(informations)
  }
}
