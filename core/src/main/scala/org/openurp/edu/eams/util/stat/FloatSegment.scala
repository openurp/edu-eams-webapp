package org.openurp.edu.eams.util.stat




import org.beangle.commons.lang.Objects
import FloatSegment._



object FloatSegment {

  def buildSegments(start: Int, span: Int, count: Int): List[FloatSegment] = {
    val segmentList = new ArrayList[FloatSegment]()
    for (i <- 0 until count) {
      segmentList.add(new FloatSegment(start, start + span - 1))
      start += span
    }
    segmentList
  }

  def countSegments(segs: List[FloatSegment], numbers: List[Number]) {
    var iter = numbers.iterator()
    while (iter.hasNext) {
      val number = iter.next()
      if (null == number) //continue
      var iterator = segs.iterator()
      while (iterator.hasNext) {
        val element = iterator.next()
        if (element.add(number.floatValue())) //break
      }
    }
  }
}

class FloatSegment(var min: Float, var max: Float) extends Comparable[Any] {

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

  def getCount(): Int = count

  def getMax(): Float = max

  def setMax(max: Float) {
    this.max = max
  }

  def getMin(): Float = min

  def setMin(min: Float) {
    this.min = min
  }

  def setCount(count: Int) {
    this.count = count
  }

  def compareTo(`object`: AnyRef): Int = {
    val myClass = `object`.asInstanceOf[FloatSegment]
    java.lang.Float.compare(myClass.min, this.min)
  }

  def clone(): AnyRef = new FloatSegment(getMin, getMax)

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
