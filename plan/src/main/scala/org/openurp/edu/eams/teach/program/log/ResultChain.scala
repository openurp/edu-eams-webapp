package org.openurp.edu.eams.teach.program.log


import ResultChain._
//remove if not needed


object ResultChain {

  object Type extends Enumeration {

    val SUCCESS = new Type()

    val FAILED = new Type()

    class Type extends Val

    implicit def convertValue(v: Value): Type = v.asInstanceOf[Type]
  }
}

class ResultChain(someProperties: Map[String, String]) extends PhaseChain(someProperties) {



  def success(): AfterChain = {
    informations.put(MajorPlanLogHelper.RESULT, Type.SUCCESS.toString)
    new AfterChain(informations)
  }

  def failed(): InfoChain = {
    informations.put(MajorPlanLogHelper.RESULT, Type.FAILED.toString)
    new InfoChain(informations)
  }
}
