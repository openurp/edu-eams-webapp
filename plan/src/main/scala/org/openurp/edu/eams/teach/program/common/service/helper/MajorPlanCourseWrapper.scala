package org.openurp.edu.eams.teach.program.common.service.helper

import java.util.Comparator
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.functor.Transformer
import org.openurp.edu.teach.plan.MajorPlanCourse
import MajorPlanCourseWrapper._

//remove if not needed


object MajorPlanCourseWrapper {

  val WRAPPER = new WrapperTransformer()

  val UNWRAPPER = new UnWrapperTransformer()

  val COMPARATOR = new MajorPlanCourseComparator()

  private class WrapperTransformer extends Transformer {

    def apply(`object`: AnyRef): AnyRef = {
      if (classOf[MajorPlanCourse].isAssignableFrom(`object`.getClass)) {
        return new MajorPlanCourseWrapper(`object`.asInstanceOf[MajorPlanCourse])
      }
      throw new IllegalArgumentException("cannot accept object other than type of MajorPlanCourse")
    }
  }

  private class UnWrapperTransformer extends Transformer {

    def apply(`object`: AnyRef): AnyRef = {
      if (`object`.getClass == classOf[MajorPlanCourseWrapper]) {
        return `object`.asInstanceOf[MajorPlanCourseWrapper].getMajorPlanCourse
      }
      throw new IllegalArgumentException("cannot accept object other than type of MajorPlanCourseWrapper")
    }
  }

  private class MajorPlanCourseComparator extends Comparator[MajorPlanCourse] {

    def compare(left: MajorPlanCourse, right: MajorPlanCourse): Int = {
      Objects.compareBuilder().add(left.getCourse.getCode, right.getCourse.getCode)
        .add(left.getCourse.getName, right.getCourse.getName)
        .add(left.getCourse.getCredits, right.getCourse.getCredits)
        .add(left.getDepartment.id, right.getDepartment.id)
        .add(left.getTerms, right.getTerms)
        .add(left.isCompulsory, right.isCompulsory)
        .toComparison()
    }
  }
}

class MajorPlanCourseWrapper(`object`: MajorPlanCourse) extends Comparable[_] {

  
  var majorPlanCourse: MajorPlanCourse = `object`

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + 
      (if ((majorPlanCourse == null)) 0 else majorPlanCourse.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (obj == null) return false
    if (getClass != obj.getClass) return false
    val other = obj.asInstanceOf[MajorPlanCourseWrapper]
    if (majorPlanCourse.getCourse == null) {
      if (other.majorPlanCourse.getCourse != null) return false
    } else if (majorPlanCourse.getCourse != other.majorPlanCourse.getCourse) return false
    if (majorPlanCourse.getRemark == null) {
      if (other.majorPlanCourse.getRemark != null) return false
    } else if (majorPlanCourse.getRemark != other.majorPlanCourse.getRemark) return false
    if (majorPlanCourse.getDepartment == null) {
      if (other.majorPlanCourse.getDepartment != null) return false
    } else if (majorPlanCourse.getDepartment != other.majorPlanCourse.getDepartment) return false
    if (majorPlanCourse.getTerms == null) {
      if (other.majorPlanCourse.getTerms != null) return false
    } else if (majorPlanCourse.getTerms != other.majorPlanCourse.getTerms) return false
    true
  }

  def compareTo(`object`: AnyRef): Int = {
    val myClass = `object`.asInstanceOf[MajorPlanCourseWrapper]
    Objects.compareBuilder().add(majorPlanCourse.getCourse.getCode, myClass.getMajorPlanCourse.getCourse.getCode)
      .add(majorPlanCourse.getCourse.getName, myClass.getMajorPlanCourse.getCourse.getName)
      .add(majorPlanCourse.getCourse.getCredits, myClass.getMajorPlanCourse.getCourse.getCredits)
      .toComparison()
  }

  override def toString(): String = {
    "MajorPlanCourseWrapper [majorPlanCourse=" + majorPlanCourse + 
      "]"
  }
}
