package org.openurp.edu.eams.teach.election.model

import javax.persistence.Entity
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject
import ElectMailTemplate._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object ElectMailTemplate {

  val WITHDRAW = 1L
}

@SerialVersionUID(-4430290657221915091L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.ElectMailTemplate")
class ElectMailTemplate extends LongIdObject {

  @NotNull
  @BeanProperty
  var title: String = _

  @NotNull
  @BeanProperty
  var content: String = _
}
