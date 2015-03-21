package org.openurp.edu.eams.util.stat








import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Objects
import StatGroup._



object StatGroup {

  def buildStatGroups(datas: List[_]): List[_] = buildStatGroups(datas, 1)

  def buildStatGroup(datas: List[_], counters: Int): StatGroup = {
    val result = new StatGroup(null)
    if (!(datas == null || datas.isEmpty)) {
      var iter = datas.iterator
      while (iter.hasNext) {
        val data = iter.next().asInstanceOf[Array[Any]]
        result.addData(data, 0, data.length - 1, counters)
      }
    }
    result
  }

  def buildStatGroups(datas: List[_], counters: Int): List[_] = {
    val stats = new ArrayList()
    if (!CollectUtils.isEmpty(datas)) {
      val rs = new HashMap()
      var iter = datas.iterator
      while (iter.hasNext) {
        val data = iter.next().asInstanceOf[Array[Any]]
        var result = rs.get(data(0)).asInstanceOf[StatGroup]
        if (null == result) {
          result = new StatGroup(data(0))
          rs.put(data(0), result)
          stats.add(result)
        }
        result.addData(data, 1, data.length - 1, counters)
      }
    }
    stats
  }
}

class StatGroup(entity: AnyRef) extends StatCountor {

  var what: AnyRef = entity

  var items: List[_] = new ArrayList()

  def this(entity: AnyRef, items: List[_]) {
    this(entity)
    this.items = items
  }

  def addData(data: Array[Any], 
      from: Int, 
      to: Int, 
      counters: Int) {
    if (to - from == counters) {
      val statItem = new StatItem(data(from))
      val cts = Array.ofDim[Comparable[_]](counters)
      System.arraycopy(data, from + 1, cts, 0, counters)
      statItem.countors=cts
      this.items.add(statItem)
    } else if (to - from > counters) {
      var subItem = new StatGroup(data(from))
      val index = items.indexOf(subItem)
      if (-1 == index) {
        items.add(subItem)
      } else {
        subItem = items.get(index).asInstanceOf[StatGroup]
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

  def getWhat(): AnyRef = what

  def setWhat(entity: AnyRef) {
    this.what = entity
  }

  def getItems(): List[_] = items

  def setItems(items: List[_]) {
    this.items = items
  }

  def getItemEntities(): List[_] = {
    val entities = new ArrayList()
    var iter = items.iterator
    while (iter.hasNext) {
      val obj = iter.next().asInstanceOf[StatCountor]
      entities.add(obj.what)
    }
    entities
  }

  def getSubItemEntities(): List[_] = {
    val entities = new HashSet()
    if (items.isEmpty) return CollectUtils.newArrayList()
    if (items.get(0).isInstanceOf[StatGroup]) {
      var iter = items.iterator
      while (iter.hasNext) {
        val obj = iter.next().asInstanceOf[AnyRef]
        entities.addAll(obj.asInstanceOf[StatGroup].itemEntities)
      }
    }
    new ArrayList(entities)
  }

  def getItem(statWhat: AnyRef): AnyRef = {
    var iter = items.iterator
    while (iter.hasNext) {
      val element = iter.next().asInstanceOf[StatCountor]
      if (Objects.==(element.what, statWhat)) {
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
      if (null != item.countors()(counterIndex)) {
        sum += (item.countors()(counterIndex)).asInstanceOf[Number]
          .doubleValue()
      }
    }
    new java.lang.Double(sum)
  }
}
