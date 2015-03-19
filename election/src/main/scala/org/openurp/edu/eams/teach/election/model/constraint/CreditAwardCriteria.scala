package org.openurp.edu.eams.teach.election.model.constraint

import javax.persistence.Entity
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject




@SerialVersionUID(8574528313999902227L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.constraint.CreditAwardCriteria")
class CreditAwardCriteria extends LongIdObject {

  @NotNull
  
  var floorAvgGrade: Float = _

  @NotNull
  
  var ceilAvgGrade: Float = _

  @NotNull
  
  var awardCredits: Float = _
}
