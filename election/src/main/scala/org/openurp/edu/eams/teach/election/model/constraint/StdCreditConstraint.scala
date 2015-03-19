package org.openurp.edu.eams.teach.election.model.constraint

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.hibernate.annotations.NaturalId
import org.openurp.base.Semester
import org.openurp.edu.base.Student




@SerialVersionUID(-6627564288570998553L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.constraint.StdCreditConstraint")
class StdCreditConstraint extends AbstractCreditConstraint {

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  
  var std: Student = _

  
  var GPA: java.lang.Float = _
}
