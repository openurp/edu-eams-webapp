package org.openurp.edu.eams.teach.grade.course.model

import javax.persistence.Entity
import org.beangle.commons.entity.pojo.LongIdObject
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(428221810871042136L)
@Entity(name = "org.openurp.edu.eams.teach.grade.course.model.ScoreSection")
class ScoreSection extends LongIdObject {

  @BeanProperty
  var fromScore: Float = _

  @BeanProperty
  var toScore: Float = _
}
