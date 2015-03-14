package org.openurp.edu.eams.teach.election.model.exception


import scala.collection.JavaConversions._

@SerialVersionUID(-2937891371653678238L)
class ElectCourseLimitCountException extends Exception() {

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
