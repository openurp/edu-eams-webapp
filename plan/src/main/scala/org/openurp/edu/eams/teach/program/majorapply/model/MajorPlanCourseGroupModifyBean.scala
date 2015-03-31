package org.openurp.edu.eams.teach.program.majorapply.model

import java.util.Date
import javax.persistence.CascadeType



import javax.persistence.OneToOne
import javax.persistence.Table
import org.beangle.data.model.bean.LongIdBean
import org.beangle.security.blueprint.User
import com.ekingstar.eams.base.Department
import org.openurp.edu.eams.teach.program.majorapply.model.component.FakePlan
import MajorCourseGroupModifyBean._

//remove if not needed


object MajorCourseGroupModifyBean {

  var INITREQUEST: java.lang.Integer = new java.lang.Integer(-1)

  var REFUSE: java.lang.Integer = new java.lang.Integer(0)

  var ACCEPT: java.lang.Integer = new java.lang.Integer(1)

  var MODIFY: String = "变动"

  var ADD: String = "增加"

  var DELETE: String = "删除"

  var REQUISITIONTYPE: Array[String] = Array(MODIFY, ADD, DELETE)
}

@SerialVersionUID(5737589654235506632L)

@Table(name = "T_MAJOR_PLAN_CG_MODIFIES")
class MajorCourseGroupModifyBean extends LongIdBean {

  
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

  
  
  var proposer: User = _

  @OneToOne(optional = true, cascade = Array(CascadeType.ALL), orphanRemoval = true)
  
  var oldPlanCourseGroup: MajorCourseGroupModifyDetailBeforeBean = _

  @OneToOne(optional = true, cascade = Array(CascadeType.ALL), orphanRemoval = true)
  
  var newPlanCourseGroup: MajorCourseGroupModifyDetailAfterBean = _

  
  
  var assessor: User = _
}
