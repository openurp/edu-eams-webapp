package org.openurp.edu.eams.util.stat

class CountItem(var count: Number, var what: AnyRef) extends Ordered[CountItem] {

  override def compare(arg0: CountItem): Int = {
    count.intValue() - arg0.count.intValue()
  }
}
