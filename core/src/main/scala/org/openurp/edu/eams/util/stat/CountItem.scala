package org.openurp.edu.eams.util.stat


import scala.collection.JavaConversions._

class CountItem(var count: Number, var what: AnyRef) extends Comparable[_] {

  def compareTo(arg0: AnyRef): Int = {
    count.intValue() - arg0.asInstanceOf[CountItem].count.intValue()
  }

  def getCount(): Number = count

  def setCount(count: java.lang.Integer) {
    this.count = count
  }

  def getWhat(): AnyRef = what

  def setWhat(what: AnyRef) {
    this.what = what
  }
}
