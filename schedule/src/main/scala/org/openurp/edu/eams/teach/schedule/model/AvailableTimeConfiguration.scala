package org.openurp.edu.eams.teach.schedule.model

import org.beangle.commons.entity.pojo.LongIdObject




@SerialVersionUID(3240925805188364485L)
class AvailableTimeConfiguration extends LongIdObject() {

  
  var name: String = _

  
  var availTime: String = _

  
  var isDefault: java.lang.Boolean = false

  def this(name: String, availTime: String, isDefault: java.lang.Boolean) {
    super()
    this.name = name
    this.availTime = availTime
    this.isDefault = isDefault
  }
}
