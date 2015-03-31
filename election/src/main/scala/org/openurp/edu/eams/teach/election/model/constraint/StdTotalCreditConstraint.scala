package org.openurp.edu.eams.teach.election.model.constraint




import javax.persistence.Table

import org.openurp.edu.base.Student




@SerialVersionUID(-2522394689697765724L)

@Table(name = "T_STD_TOTAL_CREDIT_CONS")
class StdTotalCreditConstraint extends AbstractCreditConstraint {

  
  
  
  var std: Student = _
}
