package org.openurp.edu.eams.weekstate

import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class YearWeekState extends BasicWeekState() {

  @BeanProperty
  var year: java.lang.Integer = _

  def this(state: YearWeekState) {
    super(state)
    this.year = state.getYear
  }

  protected override def clone(): YearWeekState = {
    super.clone().asInstanceOf[YearWeekState]
  }

  override def toString(): String = {
    super.getToStringBuilder.add("year", this.year).toString
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = super.hashCode
    result = prime * result + (if ((year == null)) 0 else year.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (super != obj) return false
    if (getClass != obj.getClass) return false
    val other = obj.asInstanceOf[YearWeekState]
    if (year == null) {
      if (other.year != null) return false
    } else if (year != other.year) return false
    true
  }
}
