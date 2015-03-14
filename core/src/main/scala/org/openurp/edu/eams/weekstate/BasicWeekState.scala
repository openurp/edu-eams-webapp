package org.openurp.edu.eams.weekstate

import org.openurp.edu.eams.weekstate.WeekStateDirection.LTR
import org.openurp.edu.eams.weekstate.WeekStateDirection.RTL
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Objects.ToStringBuilder
import org.openurp.edu.eams.date.EamsWeekday
import BasicWeekState._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object BasicWeekState {

  val MAX_LENGTH = 53
}

abstract class BasicWeekState extends Cloneable() {

  @BeanProperty
  var direction: WeekStateDirection = _

  @BeanProperty
  var string: String = _

  @BeanProperty
  var number: java.lang.Long = _

  @BeanProperty
  var weekday: EamsWeekday = _

  def this(state: BasicWeekState) {
    this()
    this.direction = state.direction
    this.number = state.number
    this.string = state.string
    this.weekday = state.weekday
  }

  def setWeekState(weekStateString: String) {
    this.string = weekStateString
    this.number = BinaryConverter.toLong(weekStateString)
  }

  def setWeekState(weekStateNumber: java.lang.Long) {
    this.string = BinaryConverter.toString(weekStateNumber)
    this.number = weekStateNumber
  }

  def convert2LTR(): BasicWeekState = {
    if (this.direction == LTR) {
      return clone()
    }
    reverse()
  }

  def convert2RTL(): BasicWeekState = {
    if (this.direction == RTL) {
      return clone()
    }
    reverse()
  }

  private def reverse(): BasicWeekState = {
    val res = this.clone()
    if (this.direction == RTL) {
      res.setDirection(LTR)
      res.setWeekState(new StringBuilder(this.string).reverse().toString)
    } else {
      res.setDirection(RTL)
      res.setWeekState(new StringBuilder(this.string).reverse().toString.replaceAll("^0+1", "1"))
    }
    res
  }

  protected override def clone(): BasicWeekState = {
    super.clone().asInstanceOf[BasicWeekState]
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + 
      (if ((direction == null)) 0 else direction.hashCode)
    result = prime * result + (if ((number == null)) 0 else number.hashCode)
    result = prime * result + (if ((string == null)) 0 else string.hashCode)
    result = prime * result + (if ((weekday == null)) 0 else weekday.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (obj == null) return false
    if (getClass != obj.getClass) return false
    val other = obj.asInstanceOf[BasicWeekState]
    if (direction != other.direction) return false
    if (number == null) {
      if (other.number != null) return false
    } else if (number != other.number) return false
    if (string == null) {
      if (other.string != null) return false
    } else if (string != other.string) return false
    if (weekday != other.weekday) return false
    true
  }

  override def toString(): String = getToStringBuilder.toString

  protected def getToStringBuilder(): ToStringBuilder = {
    Objects.toStringBuilder(this.getClass.getSimpleName)
      .add("direction", direction)
      .add("weekday", weekday)
      .add("string", string)
      .add("number", number)
  }
}
