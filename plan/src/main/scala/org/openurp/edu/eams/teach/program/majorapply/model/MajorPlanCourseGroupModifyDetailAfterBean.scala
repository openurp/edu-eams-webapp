package org.openurp.edu.eams.teach.program.majorapply.model

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
//remove if not needed
import scala.collection.JavaConversions._

@SerialVersionUID(7799663739549705026L)
@Entity(name = "org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailAfterBean")
@Table(name = "T_MAJOR_PLAN_CG_MOD_AFTERS")
class MajorPlanCourseGroupModifyDetailAfterBean extends MajorPlanCourseGroupModifyDetailBean() {

  @OneToOne(optional = false, targetEntity = classOf[MajorPlanCourseGroupModifyBean], mappedBy = "newPlanCourseGroup")
  @JoinColumn(name = "MA_PLAN_CG_MOD_A_APPLY")
  protected var apply: MajorPlanCourseGroupModifyBean = _

  def getApply(): MajorPlanCourseGroupModifyBean = apply

  def setApply(apply: MajorPlanCourseGroupModifyBean) {
    this.apply = apply
  }
}
