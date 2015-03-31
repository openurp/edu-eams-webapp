package org.openurp.edu.eams.teach.schedule.model

import org.beangle.data.model.bean.LongIdBean




@SerialVersionUID(3240925805188364485L)
class AvailableTimeConfiguration extends LongIdBean() {

  
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
