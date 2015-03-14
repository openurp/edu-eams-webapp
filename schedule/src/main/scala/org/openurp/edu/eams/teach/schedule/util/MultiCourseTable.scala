package org.openurp.edu.eams.teach.schedule.util

import java.util.Collections
import java.util.List
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.entity.Entity

import scala.collection.JavaConversions._

class MultiCourseTable {

  var tables: List[CourseTable] = CollectUtils.newArrayList()

  var resources: List[Entity[_]] = CollectUtils.newArrayList()

  var order: Order = _

  def getOrder(): Order = order

  def setOrder(order: Order) {
    this.order = order
  }

  def getTables(): List[CourseTable] = tables

  def setTables(tables: List[CourseTable]) {
    this.tables = tables
  }

  def getResources(): List[Entity[_]] = resources

  def setResources(resources: List[Entity[_]]) {
    this.resources = resources
  }

  def sortTables() {
    if (null != order) {
      Collections.sort(tables, new PropertyComparator(order.getProperty, order.isAscending))
    }
  }
}
