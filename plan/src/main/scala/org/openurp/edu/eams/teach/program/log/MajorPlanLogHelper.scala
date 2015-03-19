package org.openurp.edu.eams.teach.program.log



import org.openurp.edu.eams.teach.program.major.model.MajorPlanBean
//remove if not needed


object MajorPlanLogHelper {

  val PLAN_ID = "PLAN.ID"

  val PLAN_NAME = "PLAN.NAME"

  val TYPE = "TYPE"

  val RESULT = "RESULT"

  val OBJECT_ID = "OBJECT.ID"

  val OBJECT_NAME = "OBJECT.NAME"

  val OBJECT_CODE = "OBJECT.CODE"

  val BEFORE = "BEFORE"

  val AFTER = "AFTER"

  val INFO = "INFO"

  val LOG_FIELDS = Array(PLAN_ID, PLAN_NAME, TYPE, RESULT, OBJECT_ID, OBJECT_NAME, OBJECT_CODE, BEFORE, AFTER, INFO)

  private def getEmptyInfoMap(): Map[String, String] = {
    val empty = new HashMap[String, String]()
    empty.put(PLAN_ID, "")
    empty.put(PLAN_NAME, "")
    empty.put(TYPE, "")
    empty.put(RESULT, "")
    empty.put(OBJECT_ID, "")
    empty.put(OBJECT_NAME, "")
    empty.put(OBJECT_CODE, "")
    empty.put(BEFORE, "")
    empty.put(AFTER, "")
    empty.put(INFO, "")
    empty
  }

  def create(): StartCreateChain = new StartCreateChain(getEmptyInfoMap)

  def delete(): StartDeleteChain = new StartDeleteChain(getEmptyInfoMap)

  def update(): StartUpdateChain = new StartUpdateChain(getEmptyInfoMap)

  def main(args: Array[String]) {
    println(MajorPlanLogHelper.create().start().before(new MajorPlanBean())
      .success()
      .after(new MajorPlanBean())
      .info("")
      .getLogs)
  }
}
