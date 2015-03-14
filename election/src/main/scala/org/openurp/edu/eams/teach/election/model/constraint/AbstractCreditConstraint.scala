package org.openurp.edu.eams.teach.election.model.constraint

import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(6763813672438837820L)
@MappedSuperclass
abstract class AbstractCreditConstraint extends LongIdObject {

  @NotNull
  @BeanProperty
  var maxCredit: java.lang.Float = _
}
