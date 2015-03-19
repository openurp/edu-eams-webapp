package org.openurp.edu.eams.web.view.component

import java.text.DecimalFormat
import java.text.NumberFormat
import org.beangle.struts2.view.component.ClosingUIBean
import com.opensymphony.xwork2.util.ValueStack




class NumTextfield(stack: ValueStack) extends ClosingUIBean(stack) {

  
  var name: String = _

  
  var label: String = _

  
  var title: String = _

  
  var comment: String = _

  
  var check: String = _

  
  var format: String = _

  
  var required: AnyRef = _

  
  var value: AnyRef = ""

  
  var min: String = _

  
  var max: String = _

  if (null == this.id) generateIdIfEmpty()

  def evalParams() {
    super.evaluateParams()
    if (null == this.name) this.name = this.id
    required = if (null == required) false else "1" == required.toString || "true" == required.toString
    if (null != format) {
      val numberFormat = new DecimalFormat(format)
      this.value = numberFormat.format(numberFormat.parseObject(value.toString))
    }
  }
}
