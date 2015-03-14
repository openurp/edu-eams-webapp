package org.openurp.edu.eams.teach.grade.service.stat

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Student
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(5594296033065495823L)
@Entity(name = "org.openurp.edu.eams.teach.grade.service.stat.StdTermCredit")
class StdTermCredit extends LongIdObject {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var std: Student = _

  @BeanProperty
  var totalCredits: Float = _

  @BeanProperty
  var credits: Float = _
}
