package org.openurp.edu.eams.teach.program.log


//remove if not needed


class EndChain(someProperties: Map[String, String]) extends PhaseChain(someProperties) {

  def end(): String = getLogs

  def getLogs(): String = {
    val sb = new StringBuilder()
    for (i <- 0 until MajorPlanLogHelper.LOG_FIELDS.length) {
      sb.append(MajorPlanLogHelper.LOG_FIELDS(i)).append("=")
        .append(informations.get(MajorPlanLogHelper.LOG_FIELDS(i)))
        .append("\n")
    }
    sb.toString
  }
}
