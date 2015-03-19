package org.openurp.edu.eams.web.view.component

import java.io.Writer


import org.beangle.struts2.view.component.IterableUIBean
import com.opensymphony.xwork2.util.ValueStack




abstract class HierarchyUIBean[T <: HierarchyUIBean[_]](stack: ValueStack) extends IterableUIBean(stack) {

  
  var parent: T = _

  
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
