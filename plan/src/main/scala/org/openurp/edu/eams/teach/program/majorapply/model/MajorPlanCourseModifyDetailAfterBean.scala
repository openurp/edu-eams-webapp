package org.openurp.edu.eams.teach.program.majorapply.model



import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection

import javax.persistence.JoinColumn
import javax.persistence.MapKeyColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.CourseHour
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


@SerialVersionUID(7799663739549705026L)

@Table(name = "T_MAJOR_PLAN_C_MOD_AFTERS")
class MajorPlanCourseModifyDetailAfterBean extends MajorPlanCourseModifyDetailBean() {

  @OneToOne(optional = false, mappedBy = "newPlanCourse")
  @JoinColumn(name = "MA_PLAN_C_MOD_A_APPLY")
  protected var apply: MajorPlanCourseModifyBean = _

  @ElementCollection
  @MapKeyColumn(name = "COURSE_HOUR_TYPE_ID")
  @Column(name = "COURSE_HOUR")
  @CollectionTable(joinColumns = @JoinColumn(name = "MAJOR_PLAN_C_MOD_AFT_ID"), name = "T_MAJOR_PLAN_C_MOD_AFT_C_H")
  protected var courseHours: Map[Integer, Integer] = new HashMap[Integer, Integer]()

  def this(course: Course, courseGroup: MajorCourseGroup) {
    super()
    setCourse(course)
    setFakeCourseGroupByReal(courseGroup)
    val courseHourMap = new HashMap[Integer, Integer]()
    for (courseHour <- course.getHours) {
      courseHourMap.put(courseHour.getType.id, courseHour.getPeriod)
    }
    getCourseHours.putAll(courseHourMap)
  }

  def this(planCourse: MajorPlanCourse) {
    this()
    setCourse(planCourse.getCourse)
    setFakeCourseGroupByReal(planCourse.getCourseGroup.asInstanceOf[MajorCourseGroup])
    setRemark(planCourse.getRemark)
    setDepartment(planCourse.getDepartment)
    setTerms(planCourse.getTerms)
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
