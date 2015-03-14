package org.openurp.edu.eams.web.view.component

import org.beangle.commons.lang.Strings
import org.beangle.struts2.view.component.ClosingUIBean
import com.opensymphony.xwork2.util.ValueStack
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class SemesterBar(stack: ValueStack) extends ClosingUIBean(stack) {

  @BeanProperty
  var formName: String = _

  @BeanProperty
  var action: String = _

  @BeanProperty
  var target: String = _

  @BeanProperty
  var semesterName: String = _

  @BeanProperty
  var name: String = _

  @BeanProperty
  var onChange: String = _

  @BeanProperty
  var onSemesterChange: String = _

  @BeanProperty
  var initCallback: String = _

  @BeanProperty
  var label: String = _

  @BeanProperty
  var submitValue: String = _

  @BeanProperty
  var value: String = _

  @BeanProperty
  var divId: String = _

  @BeanProperty
  var submit: AnyRef = _

  @BeanProperty
  var semesterEmpty: AnyRef = _

  @BeanProperty
  var empty: AnyRef = _

  @BeanProperty
  var semesterValue: AnyRef = _

  protected override def evaluateParams() {
    if (Strings.isEmpty(this.id)) {
      generateIdIfEmpty()
    }
  }
}
