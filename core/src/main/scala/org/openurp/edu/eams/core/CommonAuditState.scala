package org.openurp.edu.eams.core

object CommonAuditState extends Enumeration {

  val UNSUBMITTED = new CommonAuditState("未提交")

  val SUBMITTED = new CommonAuditState("已提交")

  val ACCEPTED = new CommonAuditState("通过")

  val REJECTED = new CommonAuditState("不通过")

  class CommonAuditState(val fullName: String) extends Val {

    def getEngName(): String = fullName

    def getName(): String = fullName
  }

  import scala.language.implicitConversions

  implicit def convertValue(v: Value): CommonAuditState = v.asInstanceOf[CommonAuditState]
}
