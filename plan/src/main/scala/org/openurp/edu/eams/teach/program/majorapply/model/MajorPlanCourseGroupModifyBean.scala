package org.openurp.edu.eams.teach.program.majorapply.model

import java.util.Date
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table
import org.beangle.commons.entity.pojo.LongIdObject
import org.beangle.security.blueprint.User
import com.ekingstar.eams.base.Department
import org.openurp.edu.eams.teach.program.majorapply.model.component.FakePlan
import MajorPlanCourseGroupModifyBean._
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

object MajorPlanCourseGroupModifyBean {

  var INITREQUEST: java.lang.Integer = new java.lang.Integer(-1)

  var REFUSE: java.lang.Integer = new java.lang.Integer(0)

  var ACCEPT: java.lang.Integer = new java.lang.Integer(1)

  var MODIFY: String = "变动"

  var ADD: String = "增加"

  var DELETE: String = "删除"

  var REQUISITIONTYPE: Array[String] = Array(MODIFY, ADD, DELETE)
}

@SerialVersionUID(5737589654235506632L)
@Entity(name = "org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean")
@Table(name = "T_MAJOR_PLAN_CG_MODIFIES")
class MajorPlanCourseGroupModifyBean extends LongIdObject {

  @BeanProperty
  var requisitionType: String = _

  @BeanProperty
  var majorPlan: FakePlan = _

  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var department: Department = _

  @BeanProperty
  var flag: java.lang.Integer = INITREQUEST

  @BeanProperty
  var reason: String = _

  @BeanProperty
  var applyDate: Date = new Date(System.currentTimeMillis())

  @BeanProperty
  var replyDate: Date = _

  @BeanProperty
  var depOpinion: String = _

  @BeanProperty
  var teachOpinion: String = _

  @BeanProperty
  var depSign: String = _

  @BeanProperty
  var teachSign: String = _

  @BeanProperty
  var practiceSign: String = _

  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var proposer: User = _

  @OneToOne(optional = true, cascade = Array(CascadeType.ALL), orphanRemoval = true)
  @BeanProperty
  var oldPlanCourseGroup: MajorPlanCourseGroupModifyDetailBeforeBean = _

  @OneToOne(optional = true, cascade = Array(CascadeType.ALL), orphanRemoval = true)
  @BeanProperty
  var newPlanCourseGroup: MajorPlanCourseGroupModifyDetailAfterBean = _

  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var assessor: User = _
}
