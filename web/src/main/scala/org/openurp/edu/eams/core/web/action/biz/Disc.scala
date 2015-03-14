package org.openurp.edu.eams.core.web.action.biz

import java.util.ArrayList
import java.util.Date
import java.util.List
import org.openurp.edu.eams.core.code.ministry.Discipline
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class Disc(firstdisc: Discipline) {

  @BeanProperty
  var id: java.lang.Integer = firstdisc.getId

  @BeanProperty
  var name: String = firstdisc.getName

  @BeanProperty
  var code: String = firstdisc.getCode

  @BeanProperty
  var parent: Disc = _

  @BeanProperty
  var children: List[Disc] = new ArrayList[Disc]()

  val now = new Date()

  for (child <- firstdisc.getChildren if child.getEffectiveAt.compareTo(now) <= 0 if child.getInvalidAt == null || child.getInvalidAt.compareTo(now) >= 0) {
    val t_child = new Disc(child)
    t_child.setParent(this)
    this.children.add(t_child)
  }
}
