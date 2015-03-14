package org.openurp.edu.eams.web.view.component

import java.util.LinkedHashSet
import java.util.Set
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings
import org.beangle.struts2.view.component.ClosingUIBean
import com.opensymphony.xwork2.util.ValueStack
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class Script(stack: ValueStack) extends ClosingUIBean(stack) {

  protected var `type`: String = _

  protected var defer: Boolean = _

  protected var charset: String = _

  protected var tmpl: String = _

  protected var src: String = _

  protected var require: String = _

  protected var requireCss: Set[String] = new LinkedHashSet[String]()

  protected var requireJs: Set[String] = new LinkedHashSet[String]()

  @BooleanBeanProperty
  var compressed: Boolean = true

  @BooleanBeanProperty
  var compressBody: Boolean = false

  protected override def evaluateParams() {
    val devMode = getRequestParameter("devMode")
    if (null != devMode) setCompressed(!("true" == devMode || "on" == devMode))
    if (Strings.isEmpty(this.id)) {
      generateIdIfEmpty()
    }
  }

  def getType(): String = `type`

  def setType(`type`: String) {
    this.`type` = `type`
  }

  def isDefer(): Boolean = defer

  def setDefer(defer: Boolean) {
    this.defer = defer
  }

  def getCharset(): String = charset

  def setCharset(charset: String) {
    this.charset = charset
  }

  def getTmpl(): String = tmpl

  def setTmpl(tmpl: String) {
    this.tmpl = tmpl
  }

  def getSrc(): String = src

  def setSrc(src: String) {
    this.src = src
  }

  private def requireCss(css: String) {
    requireCss.add(css)
  }

  private def requireJs(js: String) {
    requireJs.add(js)
  }

  def setRequire(require: String) {
    this.require = require
    val requireArr = Strings.split(",")
    if (!Arrays.isEmpty(requireArr)) {
      for (requireItem <- requireArr) {
        if (requireItem.toLowerCase().endsWith(".js")) {
          requireJs(requireItem)
        } else if (requireItem.toLowerCase().endsWith(".css")) {
          requireCss(requireItem)
        }
      }
    }
  }
}
