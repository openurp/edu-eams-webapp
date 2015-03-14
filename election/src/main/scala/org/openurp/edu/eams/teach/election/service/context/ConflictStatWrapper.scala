package org.openurp.edu.eams.teach.election.service.context

import java.util.List
import org.beangle.commons.collection.CollectUtils
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class ConflictStatWrapper(@BeanProperty var id: Long) {

  @BeanProperty
  var conflicts: List[Long] = CollectUtils.newArrayList()
}
