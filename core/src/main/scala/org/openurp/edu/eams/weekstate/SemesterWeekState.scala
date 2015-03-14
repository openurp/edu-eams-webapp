package org.openurp.edu.eams.weekstate

import org.openurp.edu.eams.base.Semester
import SemesterWeekState._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object SemesterWeekState {

  protected val RESERVE_BITS = 1
}

class SemesterWeekState protected () extends BasicWeekState() {

  @BeanProperty
  var reserveBits: java.lang.Integer = _

  @BeanProperty
  var semester: Semester = _

  protected def this(state: SemesterWeekState) {
    super(state)
    this.reserveBits = state.getReserveBits
    this.semester = state.getSemester
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = super.hashCode
    result = prime * result + 
      (if ((reserveBits == null)) 0 else reserveBits.hashCode)
    result = prime * result + (if ((semester == null)) 0 else semester.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (super != obj) return false
    if (getClass != obj.getClass) return false
    val other = obj.asInstanceOf[SemesterWeekState]
    if (reserveBits == null) {
      if (other.reserveBits != null) return false
    } else if (reserveBits != other.reserveBits) return false
    if (semester == null) {
      if (other.semester != null) return false
    } else if (semester != other.semester) return false
    true
  }

  protected override def clone(): SemesterWeekState = {
    super.clone().asInstanceOf[SemesterWeekState]
  }

  override def toString(): String = {
    super.getToStringBuilder.add("semester", this.semester)
      .add("reserveBits", this.reserveBits)
      .toString
  }
}
