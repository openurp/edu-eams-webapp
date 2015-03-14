package org.openurp.edu.eams.teach.program.majorapply.model

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
//remove if not needed
import scala.collection.JavaConversions._

@SerialVersionUID(6587820760564688486L)
@Entity(name = "org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailBeforeBean")
@Table(name = "T_MAJOR_PLAN_CG_MOD_BEFORS")
class MajorPlanCourseGroupModifyDetailBeforeBean extends MajorPlanCourseGroupModifyDetailBean() {

  @OneToOne(optional = false, targetEntity = classOf[MajorPlanCourseGroupModifyBean], mappedBy = "oldPlanCourseGroup")
  @JoinColumn(name = "MA_PLAN_CG_MOD_B_APPLY")
  protected var apply: MajorPlanCourseGroupModifyBean = _

  def getApply(): MajorPlanCourseGroupModifyBean = apply

  def setApply(apply: MajorPlanCourseGroupModifyBean) {
    this.apply = apply
  }
}
