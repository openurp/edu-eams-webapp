package org.openurp.edu.eams.util.stat

import java.util.Comparator
import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.collection.Order



class StatItemComparator(private var order: Order) extends Ordering[AnyRef] {

  def compare(arg0: AnyRef, arg1: AnyRef): Int = {
    var item0: StatItem = null
    var item1: StatItem = null
    if (order.ascending) {
      item0 = arg0.asInstanceOf[StatItem]
      item1 = arg1.asInstanceOf[StatItem]
    } else {
      item1 = arg0.asInstanceOf[StatItem]
      item0 = arg1.asInstanceOf[StatItem]
    }
    if (null == item0.what || null == item1.what) {
      if (null == item0) {
        1
      } else {
        -1
      }
    }
    val c0 = PropertyUtils.getProperty(item0, order.property).asInstanceOf[Comparable[Any]]
    val c1 = PropertyUtils.getProperty(item1, order.property).asInstanceOf[Comparable[Any]]
    if (null == c0 || null == c1) {
      if (null == c0) {
        1
      } else {
        -1
      }
    }
    c0.compareTo(c1)
  }
}
