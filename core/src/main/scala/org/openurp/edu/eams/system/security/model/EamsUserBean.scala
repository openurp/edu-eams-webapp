package org.openurp.edu.eams.system.security.model

import javax.persistence.Entity
import org.beangle.security.blueprint.model.UserBean
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-5965604868118060594L)
@Entity(name = "org.beangle.security.blueprint.User")
class EamsUserBean extends UserBean {

  @BooleanBeanProperty
  var defaultPasswordUpdated: Boolean = false

  @BooleanBeanProperty
  var mailVerified: Boolean = false
}
