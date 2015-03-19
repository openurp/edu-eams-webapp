package org.openurp.edu.eams.teach.election.model.constraint

import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject




@SerialVersionUID(6763813672438837820L)
@MappedSuperclass
abstract class AbstractCreditConstraint extends LongIdObject {

  @NotNull
  
  var maxCredit: java.lang.Float = _
}
