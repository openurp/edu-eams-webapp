package org.openurp.edu.eams.teach.schedule.model

import javax.persistence.Entity
import org.beangle.commons.entity.pojo.LongIdObject
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-6965891450697232446L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.ArrangeRemind")
class ArrangeRemind extends LongIdObject {

  @BooleanBeanProperty
  var open: Boolean = false
}
