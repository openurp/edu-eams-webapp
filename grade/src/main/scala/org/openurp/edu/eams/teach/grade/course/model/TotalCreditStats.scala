package org.openurp.edu.eams.teach.grade.course.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.base.Student
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-3227647149353440137L)
@Entity(name = "org.openurp.edu.eams.teach.grade.course.model.TotalCreditStats")
class TotalCreditStats extends LongIdObject {

  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var std: Student = _

  @BeanProperty
  var tolCredit: java.lang.Float = _
}
