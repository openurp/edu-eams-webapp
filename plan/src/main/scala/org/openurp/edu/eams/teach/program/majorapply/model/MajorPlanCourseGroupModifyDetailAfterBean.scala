package org.openurp.edu.eams.teach.program.majorapply.model

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
//remove if not needed


@SerialVersionUID(7799663739549705026L)
@Entity(name = "org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailAfterBean")
@Table(name = "T_MAJOR_PLAN_CG_MOD_AFTERS")
class MajorCourseGroupModifyDetailAfterBean extends MajorCourseGroupModifyDetailBean() {

  @OneToOne(optional = false, targetEntity = classOf[MajorCourseGroupModifyBean], mappedBy = "newPlanCourseGroup")
  @JoinColumn(name = "MA_PLAN_CG_MOD_A_APPLY")
  protected var apply: MajorCourseGroupModifyBean = _

  def getApply(): MajorCourseGroupModifyBean = apply

  def setApply(apply: MajorCourseGroupModifyBean) {
    this.apply = apply
  }
}
