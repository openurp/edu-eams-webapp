package org.openurp.edu.eams.web.view.component

import java.io.Writer
import java.util.ArrayList
import java.util.List
import org.beangle.struts2.view.component.IterableUIBean
import com.opensymphony.xwork2.util.ValueStack
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

abstract class HierarchyUIBean[T <: HierarchyUIBean[_]](stack: ValueStack) extends IterableUIBean(stack) {

  @BeanProperty
  var parent: T = _

  @BeanProperty
  var children: List[T] = new ArrayList[T]()

  def addChild(child: T): Boolean = children.add(child)

  protected def nextChild(): Boolean

  protected def parentNext(): Boolean

  override def start(writer: Writer): Boolean = {
    evaluateParams()
    next()
  }

  override def doEnd(writer: Writer, body: String): Boolean = {
    iterator(writer, body)
    next()
  }
}
