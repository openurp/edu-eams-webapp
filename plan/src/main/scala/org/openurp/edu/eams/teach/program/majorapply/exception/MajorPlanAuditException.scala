package org.openurp.edu.eams.teach.program.majorapply.exception

//remove if not needed


@SerialVersionUID(1526506513229987736L)
class MajorPlanAuditException extends Exception() {

  def this(message: String, cause: Throwable) {
    super(message, cause)
  }

  def this(message: String) {
    super(message)
  }

  def this(cause: Throwable) {
    super(cause)
  }
}
