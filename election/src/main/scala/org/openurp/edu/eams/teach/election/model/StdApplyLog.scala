package org.openurp.edu.eams.teach.election.model

import java.util.Date
import javax.persistence.Entity
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.eams.teach.election.model.Enum.StdApplyType
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(6692048840481469549L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.StdApplyLog")
class StdApplyLog extends LongIdObject {

  @NotNull
  @BeanProperty
  var semesterId: java.lang.Integer = _

  @NotNull
  @BeanProperty
  var stdId: java.lang.Long = _

  @NotNull
  @BeanProperty
  var stdCode: String = _

  @NotNull
  @BeanProperty
  var stdName: String = _

  @NotNull
  @BeanProperty
  var courseCredit: java.lang.Float = _

  @NotNull
  @BeanProperty
  var courseCode: String = _

  @NotNull
  @BeanProperty
  var courseName: String = _

  @NotNull
  @BeanProperty
  var ip: String = _

  @NotNull
  @BeanProperty
  var applyOn: Date = _

  @NotNull
  @BeanProperty
  var applyType: StdApplyType = _

  @NotNull
  @BeanProperty
  var resultType: Int = _

  @BeanProperty
  var remark: String = _

  @NotNull
  @BeanProperty
  var taskId: java.lang.Long = _
}
