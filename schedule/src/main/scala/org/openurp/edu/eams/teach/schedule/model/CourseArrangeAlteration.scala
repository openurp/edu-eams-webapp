package org.openurp.edu.eams.teach.schedule.model

import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import org.beangle.commons.entity.pojo.LongIdObject
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.base.Semester
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(1L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.CourseArrangeAlteration")
class CourseArrangeAlteration extends LongIdObject {

  @NotNull
  @BeanProperty
  var lessonId: java.lang.Long = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @Size(max = 500)
  @BeanProperty
  var alterationBefore: String = _

  @Size(max = 500)
  @BeanProperty
  var alterationAfter: String = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var alterBy: User = _

  @Size(max = 100)
  @BeanProperty
  var alterFrom: String = _

  @NotNull
  @BeanProperty
  var alterationAt: Date = _
}
