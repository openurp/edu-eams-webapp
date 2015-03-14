package org.openurp.edu.eams.web.view.component

import org.beangle.commons.lang.Strings
import org.beangle.struts2.view.component.Form
import org.beangle.struts2.view.component.UIBean
import com.opensymphony.xwork2.util.ValueStack
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class Code(stack: ValueStack) extends UIBean(stack) {

  protected var name: String = _

  @BeanProperty
  var value: AnyRef = _

  @BeanProperty
  var label: String = _

  protected var title: String = _

  @BeanProperty
  var keyName: String = _

  @BeanProperty
  var valueName: String = _

  protected var comment: String = _

  protected var check: String = _

  protected var required: String = _

  protected var code: String = _

  protected override def evaluateParams() {
    if (null == keyName) {
      keyName = "id"
      valueName = "name"
    }
    if (null == this.id) generateIdIfEmpty()
    if (null != label) label = getText(label)
    title = if (null != title) getText(title) else label
    val myform = findAncestor(classOf[Form])
    if (null != myform) {
      if ("true" == required) myform.addCheck(id, "require()")
      if (null != check) myform.addCheck(id, check)
    }
  }

  def getName(): String = name

  def setName(name: String) {
    this.name = name
  }

  def getTitle(): String = title

  def setTitle(title: String) {
    this.title = title
  }

  def getComment(): String = comment

  def setComment(comment: String) {
    this.comment = comment
  }

  def getCheck(): String = check

  def setCheck(check: String) {
    this.check = check
  }

  def getRequired(): String = required

  def setRequired(required: String) {
    this.required = required
  }

  def getCode(): String = code

  def setCode(code: String) {
    this.code = code
  }

  def setOption(option: String) {
    if (null != option) {
      if (Strings.contains(option, ",")) {
        keyName = Strings.substringBefore(option, ",")
        valueName = Strings.substringAfter(option, ",")
      }
    }
  }
}
