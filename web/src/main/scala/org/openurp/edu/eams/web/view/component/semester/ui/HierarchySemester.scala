package org.openurp.edu.eams.web.view.component.semester.ui

import java.util.ArrayList
import java.util.List
import org.beangle.commons.entity.HierarchyEntity
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(1306627177271312843L)
class HierarchySemester extends HierarchyEntity[HierarchySemester, Integer] {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var id: java.lang.Integer = _

  @BeanProperty
  var parent: HierarchySemester = _

  @BeanProperty
  var children: List[HierarchySemester] = new ArrayList[HierarchySemester]()

  def getIdentifier(): java.lang.Integer = id

  def isPersisted(): Boolean = false

  def isTransient(): Boolean = true

  def getIndexno(): String = null
}
