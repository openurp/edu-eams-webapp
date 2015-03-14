package org.openurp.edu.eams.teach.election.model.constraint

import javax.persistence.Entity
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(8574528313999902227L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.constraint.CreditAwardCriteria")
class CreditAwardCriteria extends LongIdObject {

  @NotNull
  @BeanProperty
  var floorAvgGrade: Float = _

  @NotNull
  @BeanProperty
  var ceilAvgGrade: Float = _

  @NotNull
  @BeanProperty
  var awardCredits: Float = _
}
