package org.openurp.edu.eams.util

import org.beangle.commons.entity.Component
import FloatRange._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object FloatRange {

  object NearType extends Enumeration {

    val OVER = new NearType()

    val BELOW = new NearType()

    val EQUAL = new NearType()

    class NearType extends Val

    implicit def convertValue(v: Value): NearType = v.asInstanceOf[NearType]
  }
}

class FloatRange extends Component {

  @BeanProperty
  var min: Float = 0f

  @BooleanBeanProperty
  var minInclusive: Boolean = true

  @BeanProperty
  var max: Float = 0f

  @BooleanBeanProperty
  var maxInclusive: Boolean = true

  def this(number: Float) {
    this()
    if (java.lang.Float.isNaN(number)) {
      throw new IllegalArgumentException("The number must not be NaN")
    }
  }

  def this(min: Float, max: Float) {
    super()
    if (java.lang.Float.isNaN(min) || java.lang.Float.isNaN(max)) {
      throw new IllegalArgumentException("The numbers must not be NaN")
    }
    if (max < min) {
      this.min = max
      this.max = min
    } else {
      this.min = min
      this.max = max
    }
  }

  def this(min: Float, 
      minInclusive: Boolean, 
      max: Float, 
      maxInclusive: Boolean) {
    this(min, max)
    this.minInclusive = minInclusive
    this.maxInclusive = maxInclusive
  }

  def minInclusive() {
    this.minInclusive = true
  }

  def minExclusive() {
    this.minInclusive = false
  }

  def inclusive() {
    this.minInclusive = true
    this.maxInclusive = true
  }

  def exclusive() {
    this.minInclusive = false
    this.maxInclusive = false
  }

  def maxInclusive() {
    this.maxInclusive = true
  }

  def maxExclusive() {
    this.maxInclusive = false
  }

  def containsFloat(value: Float): Boolean = {
    var result = true
    result = if (minInclusive) min <= value && result else min < value && result
    result = if (maxInclusive) value <= max && result else value < max && result
    result
  }



  private def containsFloat(value: Float, nearType: NearType): Boolean = {
    if (nearType == NearType.EQUAL) {
      return containsFloat(value)
    }
    if (nearType == NearType.OVER) {
      if (min == value && max > value) {
        return true
      } else if (max == value) {
        return false
      }
      return containsFloat(value)
    }
    if (min == value) {
      return false
    } else if (max == value && min < value) {
      return true
    }
    containsFloat(value)
  }

  def containsRange(range: FloatRange): Boolean = {
    if (range == null) {
      return false
    }
    if (== range) {
      return true
    }
    var result = true
    if (min == max && (!minInclusive || !maxInclusive)) {
      return false
    }
    if (range.min == range.max && (!range.minInclusive || !range.maxInclusive)) {
      return true
    }
    result = if (!range.minInclusive) result && containsFloat(range.min, NearType.OVER) else result && containsFloat(range.min)
    result = if (!range.maxInclusive) result && containsFloat(range.max, NearType.BELOW) else result && containsFloat(range.max)
    result
  }

  def overlapsRange(range: FloatRange): Boolean = {
    if (range == null) {
      return false
    }
    if (== range) {
      return true
    }
    var result = false
    if (min == max && (!minInclusive || !maxInclusive)) {
      return false
    }
    if (range.min == range.max && (!range.minInclusive || !range.maxInclusive)) {
      return false
    }
    result = if (!minInclusive) result || range.containsFloat(min, NearType.OVER) else result || range.containsFloat(min)
    result = if (!maxInclusive) result || range.containsFloat(max, NearType.BELOW) else result || range.containsFloat(max)
    result = if (!range.minInclusive) result || containsFloat(range.min, NearType.OVER) else result || containsFloat(range.min)
    result
  }

  def getDistance(): Float = max - min

  private def minIn(a: Float, 
      b: Float, 
      c: Float, 
      d: Float): Float = {
    Math.min(Math.min(Math.min(a, b), c), d)
  }

  private def maxIn(a: Float, 
      b: Float, 
      c: Float, 
      d: Float): Float = {
    Math.max(Math.max(Math.max(a, b), c), d)
  }

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + java.lang.Float.floatToIntBits(max)
    result = prime * result + (if (maxInclusive) 1231 else 1237)
    result = prime * result + java.lang.Float.floatToIntBits(min)
    result = prime * result + (if (minInclusive) 1231 else 1237)
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (obj == null) return false
    if (getClass != obj.getClass) return false
    val other = obj.asInstanceOf[FloatRange]
    if (java.lang.Float.floatToIntBits(max) != java.lang.Float.floatToIntBits(other.max)) return false
    if (maxInclusive != other.maxInclusive) return false
    if (java.lang.Float.floatToIntBits(min) != java.lang.Float.floatToIntBits(other.min)) return false
    if (minInclusive != other.minInclusive) return false
    true
  }

  override def toString(): String = {
    val sb = new StringBuilder()
    sb.append(if (minInclusive) '[' else '(').append(min)
      .append(", ")
      .append(max)
      .append(if (maxInclusive) ']' else ')')
    sb.toString
  }
}
