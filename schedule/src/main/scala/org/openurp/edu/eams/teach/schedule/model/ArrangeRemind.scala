package org.openurp.edu.eams.teach.schedule.model

import javax.persistence.Entity
import org.beangle.commons.entity.pojo.LongIdObject




@SerialVersionUID(-6965891450697232446L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.ArrangeRemind")
class ArrangeRemind extends LongIdObject {

  
  var open: Boolean = false
}
