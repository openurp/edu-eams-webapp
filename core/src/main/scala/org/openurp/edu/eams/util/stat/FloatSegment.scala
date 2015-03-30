package org.openurp.edu.eams.util.stat

import org.beangle.commons.lang.Objects
import FloatSegment._
import org.beangle.commons.collection.Collections

object FloatSegment {

  def buildSegments(s: Int, span: Int, count: Int): List[FloatSegment] = {
    var start =s
    val segmentList = Collections.newBuffer[FloatSegment]
    for (i <- 0 until count) {
      segmentList += new FloatSegment(start, start + span - 1)
      start += span
    }
    segmentList.toList
  }

  def countSegments(segs: Seq[FloatSegment], numbers: List[Number]) {
    var iter = numbers.iterator
    while (iter.hasNext) {
      val number = iter.next()
      if (null != number) {
        var iterator = segs.iterator
        var added = false
        while (iterator.hasNext && !added) {
          val element = iterator.next()
          if (element.add(number.floatValue())) {
            added = true
          }
        }
      }
    }
  }
}

class FloatSegment(var min: Float, var max: Float) extends Ordered[FloatSegment] {

  var count: Int = 0

  def this() {
    this(0, 0)
  }

  def add(score: java.lang.Float): Boolean = add(score.floatValue())

  def add(score: Float): Boolean = {
    if (score <= max && score >= min) {
      count += 1
      true
    } else {
      false
    }
  }


  def compare(myClass: FloatSegment): Int = {
    java.lang.Float.compare(myClass.min, this.min)
  }

  override def clone(): AnyRef = new FloatSegment(min, max)

  def emptySeg(): Boolean = {
    if (min == 0 && max == 0) true else false
  }

  override def toString(): String = {
    Objects.toStringBuilder(this.getClass).add("min", this.min)
      .add("max", this.max)
      .add("count", this.count)
      .toString
  }
}
