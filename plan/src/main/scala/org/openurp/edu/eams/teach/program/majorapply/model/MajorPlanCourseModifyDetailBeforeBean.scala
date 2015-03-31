package org.openurp.edu.eams.teach.program.majorapply.model



import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection

import javax.persistence.JoinColumn
import javax.persistence.MapKeyColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


@SerialVersionUID(6587820760564688486L)

@Table(name = "T_MAJOR_PLAN_C_MOD_BEFORS")
class MajorPlanCourseModifyDetailBeforeBean extends MajorPlanCourseModifyDetailBean() {

  @OneToOne(optional = false, targetEntity = classOf[MajorPlanCourseModifyBean], mappedBy = "oldPlanCourse")
  @JoinColumn(name = "MA_PLAN_C_MOD_B_APPLY")
  protected var apply: MajorPlanCourseModifyBean = _

  @ElementCollection
  @MapKeyColumn(name = "COURSE_HOUR_TYPE_ID")
  @Column(name = "COURSE_HOUR")
  @CollectionTable(joinColumns = @JoinColumn(name = "MAJOR_PLAN_C_MOD_BEF_ID"), name = "T_MAJOR_PLAN_C_MOD_BEF_C_H")
  protected var courseHours: Map[Integer, Integer] = new HashMap[Integer, Integer]()

  def this(planCourse: MajorPlanCourse) {
    super()
    setCourse(planCourse.getCourse)
    setFakeCourseGroupByReal(planCourse.getCourseGroup.asInstanceOf[MajorCourseGroup])
    setRemark(planCourse.getRemark)
    setDepartment(planCourse.getDepartment)
    setTerms(planCourse.getTerms)
    setCompulsory(planCourse.isCompulsory)
  }

  def getApply(): MajorPlanCourseModifyBean = apply

  def setApply(apply: MajorPlanCourseModifyBean) {
    this.apply = apply
  }

  def getCourseHours(): Map[Integer, Integer] = courseHours

  def setCourseHours(courseHours: Map[Integer, Integer]) {
    this.courseHours = courseHours
  }
}
