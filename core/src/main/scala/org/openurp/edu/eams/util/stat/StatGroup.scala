package org.openurp.edu.eams.util.stat

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Objects
import StatGroup._
import scala.collection.mutable.HashSet
import java.util.ArrayList
import java.util.HashMap
import scala.collection.mutable.Buffer

object StatGroup {

  def buildStatGroups(datas: List[_]): Seq[_] = buildStatGroups(datas, 1)

  def buildStatGroup(datas: List[_], counters: Int): StatGroup = {
    val result = new StatGroup(null)
    if (!(datas == null || datas.isEmpty)) {
      var iter = datas.iterator
      while (iter.hasNext) {
        val data = iter.next().asInstanceOf[Array[AnyRef]]
        result.addData(data, 0, data.length - 1, counters)
      }
    }
    result
  }

  def buildStatGroups(datas: List[_], counters: Int): Seq[_] = {
    val stats = Collections.newBuffer[Any]
    if (!Collections.isEmpty(datas)) {
      val rs = Collections.newMap[AnyRef, StatGroup]
      var iter = datas.iterator
      while (iter.hasNext) {
        val data = iter.next().asInstanceOf[Array[AnyRef]]
        var result = rs.get(data(0)).asInstanceOf[StatGroup]
        if (null == result) {
          result = new StatGroup(data(0))
          rs.put(data(0), result)
          stats += (result)
        }
        result.addData(data, 1, data.length - 1, counters)
      }
    }
    stats
  }
}

class StatGroup(entity: AnyRef) extends StatCountor {

  var what: AnyRef = entity

  var items = Collections.newBuffer[Any]

  def this(entity: AnyRef, items: Buffer[Any]) {
    this(entity)
    this.items = items
  }

  def addData(data: Array[AnyRef],
    from: Int,
    to: Int,
    counters: Int) {
    if (to - from == counters) {
      val statItem = new StatItem(data(from))
      val cts = Array.ofDim[Comparable[_]](counters)
      System.arraycopy(data, from + 1, cts, 0, counters)
      statItem.countors = cts
      this.items += statItem
    } else if (to - from > counters) {
      var subItem = new StatGroup(data(from))
      val index = items.indexOf(subItem)
      if (-1 == index) {
        items += subItem
      } else {
        subItem = items(index).asInstanceOf[StatGroup]
      }
      subItem.addData(data, from + 1, to, counters)
    } else {
      return
    }
  }

  override def equals(`object`: Any): Boolean = {
    if (!(`object`.isInstanceOf[StatGroup])) {
      return false
    }
    val rhs = `object`.asInstanceOf[StatGroup]
    Objects.==(this.what, rhs.what)
  }

  override def hashCode(): Int = {
    if ((null != what)) 629 else this.what.hashCode
  }

  def itemEntities(): Seq[Any] = {
    val entities = Collections.newBuffer[Any]
    var iter = items.iterator
    while (iter.hasNext) {
      val obj = iter.next().asInstanceOf[StatCountor]
      entities += (obj.what)
    }
    entities
  }

  def getSubItemEntities(): Seq[_] = {
    val entities = Collections.newSet[Any]
    if (items.isEmpty) return Collections.newBuffer[Any]
    if (items(0).isInstanceOf[StatGroup]) {
      var iter = items.iterator
      while (iter.hasNext) {
        val obj = iter.next().asInstanceOf[AnyRef]
        entities ++= (obj.asInstanceOf[StatGroup].itemEntities)
      }
    }
    entities.toSeq
  }

  def getItem(statWhat: AnyRef): AnyRef = {
    var iter = items.iterator
    while (iter.hasNext) {
      val element = iter.next().asInstanceOf[StatCountor]
      if (element.what == statWhat) {
        return element
      }
    }
    null
  }

  def sumItemCounter(counterIndex: Int): Number = {
    var sum = 0
    var iterator = items.iterator
    while (iterator.hasNext) {
      val item = iterator.next().asInstanceOf[StatItem]
      if (null != item.countors(counterIndex)) {
        sum += (item.countors(counterIndex)).asInstanceOf[Number].intValue
      }
    }
    new java.lang.Double(sum)
  }
}
