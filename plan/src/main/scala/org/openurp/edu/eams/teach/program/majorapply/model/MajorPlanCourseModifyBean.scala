package org.openurp.edu.eams.teach.program.majorapply.model

import java.util.Date
import javax.persistence.CascadeType



import javax.persistence.OneToOne
import javax.persistence.Table
import org.beangle.data.model.bean.LongIdBean
import org.beangle.security.blueprint.User
import com.ekingstar.eams.base.Department
import org.openurp.edu.eams.teach.program.majorapply.model.component.FakePlan
import MajorPlanCourseModifyBean._

//remove if not needed


object MajorPlanCourseModifyBean {

  var INITREQUEST: java.lang.Integer = new java.lang.Integer(-1)

  var REFUSE: java.lang.Integer = new java.lang.Integer(0)

  var ACCEPT: java.lang.Integer = new java.lang.Integer(1)

  var MODIFY: String = "变动"

  var ADD: String = "增加"

  var DELETE: String = "删除"

  var REQUISITIONTYPE: Array[String] = Array(MODIFY, ADD, DELETE)
}

@SerialVersionUID(1L)

@Table(name = "T_MAJOR_PLAN_C_MODIFIES")
class MajorPlanCourseModifyBean extends LongIdBean {

  
  var requisitionType: String = _

  
  var majorPlan: FakePlan = _

  
  
  var department: Department = _

  
  var flag: java.lang.Integer = INITREQUEST

  
  var reason: String = _

  
  var applyDate: Date = new Date(System.currentTimeMillis())

  
  var replyDate: Date = _

  
  var depOpinion: String = _

  
  var teachOpinion: String = _

  
  var depSign: String = _

  
  var teachSign: String = _

  
  var practiceSign: String = _

  @OneToOne(optional = true, cascade = Array(CascadeType.ALL), orphanRemoval = true)
  
  var oldPlanCourse: MajorPlanCourseModifyDetailBeforeBean = _

  @OneToOne(optional = true, cascade = Array(CascadeType.ALL), orphanRemoval = true)
  
  var newPlanCourse: MajorPlanCourseModifyDetailAfterBean = _

  
  
  var proposer: User = _

  
  
  var assessor: User = _
}
