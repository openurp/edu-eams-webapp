package org.openurp.edu.eams.teach.program.common.service.helper

import org.beangle.commons.lang.functor.Transformer
import com.ekingstar.eams.teach.code.school.CourseType
import CourseTypeWrapper._

//remove if not needed


object CourseTypeWrapper {

  val WRAPPER = new WrapperTransformer()

  val UNWRAPPER = new UnWrapperTransformer()

  private class WrapperTransformer extends Transformer {

    def apply(`object`: AnyRef): AnyRef = {
      if (`object`.getClass == classOf[CourseType]) {
        return new CourseTypeWrapper(`object`.asInstanceOf[CourseType])
      }
      throw new IllegalArgumentException("cannot accept object other than type of CourseType")
    }
  }

  private class UnWrapperTransformer extends Transformer {

    def apply(`object`: AnyRef): AnyRef = {
      if (`object`.getClass == classOf[CourseTypeWrapper]) {
        return `object`.asInstanceOf[CourseTypeWrapper].getCourseType
      }
      throw new IllegalArgumentException("cannot accept object other than type of CourseTypeWrapper")
    }
  }
}

class CourseTypeWrapper(`object`: CourseType) {

  
  var courseType: CourseType = `object`

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + 
      (if ((courseType == null)) 0 else courseType.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (obj == null) return false
    if (getClass != obj.getClass) return false
    val other = obj.asInstanceOf[CourseTypeWrapper]
    if (courseType == null) {
      if (other.courseType != null) return false
    } else if (courseType.id != other.courseType.id) return false
    true
  }

  override def toString(): String = {
    "CourseTypeWrapper [courseType=" + courseType + "]"
  }
}
