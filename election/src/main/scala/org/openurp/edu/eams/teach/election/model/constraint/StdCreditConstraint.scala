package org.openurp.edu.eams.teach.election.model.constraint

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.hibernate.annotations.NaturalId
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Student
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-6627564288570998553L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.constraint.StdCreditConstraint")
class StdCreditConstraint extends AbstractCreditConstraint {

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var std: Student = _

  @BeanProperty
  var GPA: java.lang.Float = _
}
