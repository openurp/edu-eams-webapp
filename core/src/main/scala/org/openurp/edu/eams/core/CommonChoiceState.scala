package org.openurp.edu.eams.core

object CommonChoiceState extends Enumeration {

  val UNDECIDED = new CommonChoiceState("未选择")

  val WANT = new CommonChoiceState("选择")

  val DONTWANT = new CommonChoiceState("不选择")

  class CommonChoiceState(val fullName: String) extends Val {

    def getEngName(): String = fullName

    def getName(): String = fullName
  }

  implicit def convertValue(v: Value): CommonChoiceState = v.asInstanceOf[CommonChoiceState]
}
