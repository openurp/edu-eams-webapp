package org.openurp.edu.eams.util.stat


import scala.collection.JavaConversions._

class StatItem(var what: AnyRef) extends StatCountor {

  var countors: Array[Comparable[_]] = _

  def this(what: AnyRef, count: Comparable[_]) {
    this()
    this.what = what
    this.countors = Array(count)
  }

  def this(what: AnyRef, 
      count1: Comparable[_], 
      count2: Comparable[_], 
      count3: Comparable[_]) {
    this()
    this.what = what
    this.countors = Array(count1, count2, count3)
  }

  def this(what: AnyRef, 
      count1: Comparable[_], 
      count2: Comparable[_], 
      count3: Comparable[_], 
      count4: Comparable[_]) {
    this()
    this.what = what
    this.countors = Array(count1, count2, count3, count4)
  }

  def this(what: AnyRef, count1: Comparable[_], count2: Comparable[_]) {
    this()
    this.what = what
    this.countors = Array(count1, count2)
  }

  def getCountors(): Array[Comparable[_]] = countors

  def setCountors(countor: Array[Comparable[_]]) {
    this.countors = countor
  }

  def getWhat(): AnyRef = what

  def setWhat(what: AnyRef) {
    this.what = what
  }
}
