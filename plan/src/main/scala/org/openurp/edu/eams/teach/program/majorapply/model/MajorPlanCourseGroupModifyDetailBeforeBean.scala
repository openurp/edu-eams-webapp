package org.openurp.edu.eams.teach.program.majorapply.model


@SerialVersionUID(6587820760564688486L)

class MajorCourseGroupModifyDetailBeforeBean extends MajorCourseGroupModifyDetailBean {
  protected var apply: MajorCourseGroupModifyBean = _

  def getApply(): MajorCourseGroupModifyBean = apply

  def setApply(apply: MajorCourseGroupModifyBean) {
    this.apply = apply
  }
}
