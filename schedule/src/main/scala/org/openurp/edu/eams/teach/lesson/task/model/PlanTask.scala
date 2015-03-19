package org.openurp.edu.eams.teach.lesson.task.model

import java.util.Date
import org.beangle.commons.entity.pojo.LongIdObject
import org.beangle.security.blueprint.User
import org.openurp.base.Semester
import org.openurp.edu.base.Course
import org.openurp.edu.teach.plan.MajorPlan
import PlanTask._




object PlanTask {

  var INIT: java.lang.Integer = java.lang.Integer.valueOf(-1)

  var REQ_CLOSE: java.lang.Integer = java.lang.Integer.valueOf(0)

  var REQ_OPEN: java.lang.Integer = java.lang.Integer.valueOf(1)
}

@SerialVersionUID(7435640814616551019L)
@Deprecated
class PlanTask extends LongIdObject {

  
  var semester: Semester = _

  private var teachPlan: MajorPlan = _

  
  var course: Course = _

  
  var applyDate: Date = new Date(System.currentTimeMillis())

  
  var replyDate: Date = _

  
  var flag: java.lang.Integer = INIT

  
  var proposer: User = _

  
  var assessor: User = _

  def getMajorPlan(): MajorPlan = teachPlan

  def setMajorPlan(teachPlan: MajorPlan) {
    this.teachPlan = teachPlan
  }
}
