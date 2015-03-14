package org.openurp.edu.eams.web.view.component

import java.text.DecimalFormat
import java.text.NumberFormat
import org.beangle.struts2.view.component.ClosingUIBean
import com.opensymphony.xwork2.util.ValueStack
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class NumTextfield(stack: ValueStack) extends ClosingUIBean(stack) {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var label: String = _

  @BeanProperty
  var title: String = _

  @BeanProperty
  var comment: String = _

  @BeanProperty
  var check: String = _

  @BeanProperty
  var format: String = _

  @BeanProperty
  var required: AnyRef = _

  @BeanProperty
  var value: AnyRef = ""

  @BeanProperty
  var min: String = _

  @BeanProperty
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
