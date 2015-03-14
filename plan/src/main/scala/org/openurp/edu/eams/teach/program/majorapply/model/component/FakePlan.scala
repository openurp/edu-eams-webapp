package org.openurp.edu.eams.teach.program.majorapply.model.component

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

@SerialVersionUID(-7881656573268919596L)
@Embeddable
class FakePlan extends Serializable {

  @Column(name = "plan_id")
  @BeanProperty
  var id: java.lang.Long = _
}
