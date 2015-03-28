package org.openurp.edu.eams.teach.election.model.Enum



import org.beangle.commons.collection.Collections



object ElectRuleType extends Enumeration {

  val ELECTION = new ElectRuleType()

  val WITHDRAW = new ElectRuleType()

  val GENERAL = new ElectRuleType()

  class ElectRuleType extends Val {

    def getCnName(): String = valueMap().get(this.toString)
  }

  def valueMap(): Map[String, String] = {
    val result = Collections.newMap[Any]
    result.put(ELECTION.toString, "选课")
    result.put(WITHDRAW.toString, "退课")
    result.put(GENERAL.toString, "登录")
    result
  }

  def strValues(): List[String] = {
    val values = values
    val result = Collections.newBuffer[Any]
    for (electRuleType <- values) {
      result.add(electRuleType.toString)
    }
    result
  }

  def getElectTypes(): Map[String, String] = {
    val result = valueMap()
    result.remove(GENERAL.toString)
    result
  }

  implicit def convertValue(v: Value): ElectRuleType = v.asInstanceOf[ElectRuleType]
}
