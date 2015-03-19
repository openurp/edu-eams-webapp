package org.openurp.edu.eams.teach.election.model

import javax.persistence.Entity
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject
import ElectMailTemplate._




object ElectMailTemplate {

  val WITHDRAW = 1L
}

@SerialVersionUID(-4430290657221915091L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.ElectMailTemplate")
class ElectMailTemplate extends LongIdObject {

  @NotNull
  
  var title: String = _

  @NotNull
  
  var content: String = _
}
