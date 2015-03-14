package org.openurp.edu.eams.teach.lesson.task.model

import java.util.Date
import org.beangle.commons.entity.pojo.LongIdObject
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.plan.MajorPlan
import PlanTask._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object PlanTask {

  var INIT: java.lang.Integer = java.lang.Integer.valueOf(-1)

  var REQ_CLOSE: java.lang.Integer = java.lang.Integer.valueOf(0)

  var REQ_OPEN: java.lang.Integer = java.lang.Integer.valueOf(1)
}

@SerialVersionUID(7435640814616551019L)
@Deprecated
class PlanTask extends LongIdObject {

  @BeanProperty
  var semester: Semester = _

  private var teachPlan: MajorPlan = _

  @BeanProperty
  var course: Course = _

  @BeanProperty
  var applyDate: Date = new Date(System.currentTimeMillis())

  @BeanProperty
  var replyDate: Date = _

  @BeanProperty
  var flag: java.lang.Integer = INIT

  @BeanProperty
  var proposer: User = _

  @BeanProperty
  var assessor: User = _

  def getMajorPlan(): MajorPlan = teachPlan

  def setMajorPlan(teachPlan: MajorPlan) {
    this.teachPlan = teachPlan
  }
}
