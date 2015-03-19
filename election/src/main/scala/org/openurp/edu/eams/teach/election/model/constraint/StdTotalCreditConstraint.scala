package org.openurp.edu.eams.teach.election.model.constraint

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull
import org.openurp.edu.base.Student




@SerialVersionUID(-2522394689697765724L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.constraint.StdTotalCreditConstraint")
@Table(name = "T_STD_TOTAL_CREDIT_CONS")
class StdTotalCreditConstraint extends AbstractCreditConstraint {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var std: Student = _
}
