package org.openurp.edu.eams.teach.program.majorapply.model





import org.apache.commons.beanutils.PropertyUtils
import org.beangle.data.model.bean.LongIdBean
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.code.school.CourseHourType
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseBean
import org.openurp.edu.eams.teach.program.majorapply.model.component.FakeCourseGroup
//remove if not needed


@SerialVersionUID(5552733977609925991L)

abstract class MajorPlanCourseModifyDetailBean extends LongIdBean with Comparable[_] with Cloneable {

  
  protected var course: Course = _

  
  protected var department: Department = _

  protected var terms: String = _

  protected var compulsory: Boolean = _

  protected var fakeCourseGroup: FakeCourseGroup = _

  protected var remark: String = _

  def isSame(`object`: AnyRef): Boolean = {
    if (!(`object`.isInstanceOf[PlanCourse])) {
      return false
    }
    val rhs = `object`.asInstanceOf[MajorPlanCourse]
    Objects.equalsBuilder().add(getTerms, rhs.getTerms)
      .add(getRemark, rhs.getRemark)
      .add(getDepartment.id, rhs.getDepartment.id)
      .add(getCourse.id, rhs.getCourse.id)
      .add(id, rhs.id)
      .isEquals
  }

  def compareTo(`object`: AnyRef): Int = {
    val myClass = `object`.asInstanceOf[PlanCourse]
    Objects.compareBuilder().add(getCourse.getCredits, myClass.getCourse.getCredits)
      .toComparison()
  }

  def clone(): AnyRef = {
    val planCourse = new MajorPlanCourseBean()
    PropertyUtils.copyProperties(planCourse, this)
    planCourse.setCourseGroup(null)
    planCourse.setId(null)
    planCourse
  }

  def inTerm(terms: String): Boolean = {
    if (Strings.isEmpty(terms)) {
      true
    } else {
      val termArray = Strings.split(terms, ",")
      for (i <- 0 until termArray.length if Strings.contains("," + getTerms + ",", "," + termArray(i) + ",")) return true
      false
    }
  }

  def getCourse(): Course = course

  def setCourse(course: Course) {
    this.course = course
  }

  def getFakeCourseGroup(): FakeCourseGroup = fakeCourseGroup

  def setFakeCourseGroup(fakeCourseGroup: FakeCourseGroup) {
    this.fakeCourseGroup = fakeCourseGroup
  }

  def setFakeCourseGroupByReal(courseGroup: MajorCourseGroup) {
    if (this.fakeCourseGroup == null) {
      this.fakeCourseGroup = new FakeCourseGroup()
    }
    this.fakeCourseGroup.setId(courseGroup.id)
    this.fakeCourseGroup.setCourseType(courseGroup.getCourseType)
  }

  def getTerms(): String = terms

  def setTerms(terms: String) {
    this.terms = terms
  }

  def getRemark(): String = remark

  def setRemark(remark: String) {
    this.remark = remark
  }

  def getCourseHours(): Map[Integer, Integer]

  def setCourseHours(courseHours: Map[Integer, Integer]): Unit

  def getCourseHour(`type`: CourseHourType): java.lang.Integer = getCourseHours.get(`type`.id)

  def getDepartment(): Department = department

  def setDepartment(department: Department) {
    this.department = department
  }

  def isCompulsory(): Boolean = compulsory

  def setCompulsory(compulsory: Boolean) {
    this.compulsory = compulsory
  }

  def getApply(): MajorPlanCourseModifyBean

  def setApply(apply: MajorPlanCourseModifyBean): Unit
}
