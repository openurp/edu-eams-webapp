package org.openurp.edu.eams.teach.program.personal.exception

import org.openurp.edu.eams.teach.program.major.MajorPlan
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

@SerialVersionUID(6670641749888531624L)
class PersonalPlanSyncException(stdMajorPlan: MajorPlan) extends Exception() {

  @BeanProperty
  var name: String = "没有找到和该生的个人培养计划匹配的专业培养计划"

  @BeanProperty
  var engName: String = "There are no Major Teach Plan matched with this Student's Teach Plan"

  @BeanProperty
  var majorPlan: MajorPlan = stdMajorPlan

  def this() {
    super()
  }

  def this(arg0: String, arg1: Throwable) {
    super(arg0, arg1)
  }

  def this(arg0: String) {
    super(arg0)
  }

  def this(arg0: Throwable) {
    super(arg0)
  }
}
