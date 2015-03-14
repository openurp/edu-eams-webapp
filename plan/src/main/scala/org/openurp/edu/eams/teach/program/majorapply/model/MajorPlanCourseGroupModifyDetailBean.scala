package org.openurp.edu.eams.teach.program.majorapply.model

import javax.persistence.Column
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import org.beangle.commons.entity.pojo.LongIdObject
import org.hibernate.annotations.Target
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.eams.teach.program.GroupRelation
import org.openurp.edu.eams.teach.program.majorapply.model.component.FakeCourseGroup
import org.openurp.edu.eams.teach.program.model.ExpressionGroupRelation
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

@SerialVersionUID(5552733977609925991L)
@MappedSuperclass
abstract class MajorPlanCourseGroupModifyDetailBean extends LongIdObject {

  @ManyToOne(fetch = FetchType.LAZY)
  protected var courseType: CourseType = _

  @Target(classOf[ExpressionGroupRelation])
  @BeanProperty
  var relation: GroupRelation = _

  protected var credits: Float = _

  protected var courseNum: Int = _

  @Column(length = 50)
  protected var termCredits: String = _

  protected var parent: FakeCourseGroup = _

  @Column(length = 500)
  protected var remark: String = _

  def getCourseType(): CourseType = courseType

  def setCourseType(courseType: CourseType) {
    this.courseType = courseType
  }

  def getCredits(): Float = credits

  def setCredits(credits: Float) {
    this.credits = credits
  }

  def getCourseNum(): Int = courseNum

  def setCourseNum(courseNum: Int) {
    this.courseNum = courseNum
  }

  def getTermCredits(): String = termCredits

  def setTermCredits(termCredits: String) {
    this.termCredits = termCredits
  }

  def getParent(): FakeCourseGroup = parent

  def setParent(parent: FakeCourseGroup) {
    this.parent = parent
  }

  def getRemark(): String = remark

  def setRemark(remark: String) {
    this.remark = remark
  }

  def getApply(): MajorPlanCourseGroupModifyBean

  def setApply(apply: MajorPlanCourseGroupModifyBean): Unit
}
