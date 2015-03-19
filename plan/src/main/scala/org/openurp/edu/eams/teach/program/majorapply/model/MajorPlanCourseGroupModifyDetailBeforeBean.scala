package org.openurp.edu.eams.teach.program.majorapply.model

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
//remove if not needed


@SerialVersionUID(6587820760564688486L)
@Entity(name = "org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailBeforeBean")
@Table(name = "T_MAJOR_PLAN_CG_MOD_BEFORS")
class MajorCourseGroupModifyDetailBeforeBean extends MajorCourseGroupModifyDetailBean() {

  @OneToOne(optional = false, targetEntity = classOf[MajorCourseGroupModifyBean], mappedBy = "oldPlanCourseGroup")
  @JoinColumn(name = "MA_PLAN_CG_MOD_B_APPLY")
  protected var apply: MajorCourseGroupModifyBean = _

  def getApply(): MajorCourseGroupModifyBean = apply

  def setApply(apply: MajorCourseGroupModifyBean) {
    this.apply = apply
  }
}
