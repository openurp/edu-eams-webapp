package org.openurp.edu.eams.web.view.component

import org.beangle.commons.lang.Strings
import org.beangle.struts2.view.component.Select
import com.opensymphony.xwork2.util.ValueStack
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class ProjectUI(stack: ValueStack) extends Select(stack) {

  @BeanProperty
  var semesterName: String = _

  @BeanProperty
  var onChange: String = _

  @BeanProperty
  var initCallback: String = _

  @BeanProperty
  var onSemesterChange: String = _

  @BeanProperty
  var semesterEmpty: AnyRef = _

  @BeanProperty
  var semesterValue: AnyRef = _

  if (Strings.isBlank(onChange)) {
    onChange = null
  }

  if (Strings.isBlank(onSemesterChange)) {
    onSemesterChange = null
  }

  if (Strings.isBlank(initCallback)) {
    initCallback = null
  }

  protected override def evaluateParams() {
    super.evaluateParams()
    if (null == semesterName) {
      semesterName = "semester"
    }
  }
}
