package org.openurp.edu.eams.system.security.model

import javax.persistence.Entity
import org.beangle.security.blueprint.model.UserBean




@SerialVersionUID(-5965604868118060594L)
@Entity(name = "org.beangle.security.blueprint.User")
class EamsUserBean extends UserBean {

  
  var defaultPasswordUpdated: Boolean = false

  
  var mailVerified: Boolean = false
}
