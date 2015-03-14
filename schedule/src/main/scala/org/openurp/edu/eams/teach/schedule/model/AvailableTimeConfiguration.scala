package org.openurp.edu.eams.teach.schedule.model

import org.beangle.commons.entity.pojo.LongIdObject
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(3240925805188364485L)
class AvailableTimeConfiguration extends LongIdObject() {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var availTime: String = _

  @BeanProperty
  var isDefault: java.lang.Boolean = false

  def this(name: String, availTime: String, isDefault: java.lang.Boolean) {
    super()
    this.name = name
    this.availTime = availTime
    this.isDefault = isDefault
  }
}
