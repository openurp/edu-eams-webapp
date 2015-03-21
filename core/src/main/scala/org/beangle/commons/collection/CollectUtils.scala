package org.beangle.commons.collection

object CollectUtils {

  def isEmpty(a: Iterable[_]): Boolean = {
    if (a == null) true
    else a.isEmpty
  }

  def isNotEmpty(a: Iterable[_]): Boolean = {
    (a != null && !a.isEmpty)
  }

  def newHashMap[A, B](): collection.mutable.HashMap[A, B] = {
    new collection.mutable.HashMap[A, B]
  }

  def newArrayList[A]: collection.mutable.ListBuffer[A] = {
    new collection.mutable.ListBuffer[A]()
  }
}