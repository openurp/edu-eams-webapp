package org.openurp.edu.eams.teach.program.majorapply.model.component

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

//remove if not needed


@SerialVersionUID(-7881656573268919596L)
@Embeddable
class FakePlan extends Serializable {

  @Column(name = "plan_id")
  
  var id: java.lang.Long = _
}
